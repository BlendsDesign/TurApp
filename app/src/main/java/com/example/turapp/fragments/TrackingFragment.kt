package com.example.turapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.GeomagneticField
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentTrackingBinding
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.utils.helperFiles.REQUEST_CODE_LOCATION_PERMISSION
import com.example.turapp.utils.locationClient.LocationService
import com.example.turapp.viewmodels.TrackingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class TrackingFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var clearSelectedMarkerOverlay: MapEventsOverlay =
        MapEventsOverlay(getClearSelectedMarkerEventsReceiver())

    lateinit var binding: FragmentTrackingBinding

    private val markersList = mutableListOf<Marker>()

    private val trekOverlay = mutableListOf<Polyline>()

    private lateinit var orientationProvider: InternalCompassOrientationProvider

    private lateinit var map: MapView // 3

    private lateinit var geoField: GeomagneticField

    private var declination: Float = 0F

    private val pathToTarget = Polyline().apply {
        color = Color.BLUE
        setPoints(mutableListOf())
    }

    private lateinit var clMark: Marker

    private val viewModel: TrackingViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, TrackingViewModel.Factory(app))[TrackingViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        if (PermissionCheckUtility.hasLocationPermissions(requireContext())) {
            viewModel.startLocationClient()
        } else {
            lifecycleScope.launch {
                var timer = 10
                while (
                    !PermissionCheckUtility.hasLocationPermissions(requireContext())
                    || timer == 10
                ) {
                    delay(1000)
                    timer++
                }
                viewModel.startLocationClient()
            }
        }
        orientationProvider = InternalCompassOrientationProvider(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(inflater)
        setUpBottomNavTrackingFragmentButtons()

        //courtesy of https://stackoverflow.com/questions/6276501/how-to-put-an-image-in-an-alertdialog-android
        binding.fabTrackingHelp.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setIcon(R.drawable.ic_help)
            val imageView = ImageView(requireContext())
            val linearLayout = LinearLayout(context)
            val textView = TextView(context)

            linearLayout.orientation = LinearLayout.VERTICAL
//            val layoutParams = (linearLayout.layoutParams as? MarginLayoutParams)
//            layoutParams?.setMargins(40, 40, 40, 40)
//            linearLayout.layoutParams = layoutParams
            val sharedPrefs = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val curLang = sharedPrefs.getString("language", "none")
            if (curLang == "English") {
                imageView.setImageResource(R.drawable.help_map_eng)
            } else { //language == "nb"
                imageView.setImageResource(R.drawable.help_map_nor)
            }


            textView.setText(R.string.help_text)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            textView.setTypeface(null, Typeface.BOLD)
            linearLayout.addView(textView)
            linearLayout.addView(imageView)


            alertDialog.setTitle("Help")
            alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL, "OK"
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            alertDialog.setView(linearLayout)
            alertDialog.show()
        }

        // Set up Map handling
        map = binding.trackingMap
        map.setDestroyMode(false)
        clMark = Marker(map).apply {
            icon = getDrawable(requireContext(), R.drawable.ic_my_location_arrow)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
        map.overlays.add(pathToTarget)
        map.overlays.add(clMark)
        lifecycleScope.launchWhenCreated {
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
            map.setMultiTouchControls(true) //3
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.controller.setZoom(18.0)
            viewModel.startingPoint.observe(viewLifecycleOwner) {
                if (it != null) {
                    map.controller.animateTo(it)
                    map.invalidate()
                }
            }
            val myMapEventsOverlay = MapEventsOverlay(getEventsReceiver())
            map.overlays.add(myMapEventsOverlay)
        }

        viewModel.myPointList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                lifecycleScope.launch {
                    // REMOVE EXISTING MARKERS
                    markersList.forEach { marker ->
                        map.overlays.remove(marker)
                    }
                    markersList.clear()

                    it.forEach { point ->
                        // Could do a check here to draw polyline if the list is multiple points
                        val temp = Marker(map)
                        temp.apply {
                            point.location?.let {
                                position = it
                            }
                            title = point.title
                            when (point.type) {
                                TYPE_POI -> icon =
                                    getDrawable(requireContext(), R.drawable.ic_marker_orange)
                                TYPE_TRACKING -> {
                                    icon =
                                        getDrawable(requireContext(), R.drawable.ic_run_circle_blue)
                                    icon.setTint(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.theme_orange
                                        )
                                    )
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                }
                                TYPE_SNAPSHOT -> {
                                    icon = getDrawable(requireContext(), R.drawable.ic_image)
                                    icon.setTint(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.theme_orange
                                        )
                                    )
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                }
                            }
                            id = point.pointId.toString()
                            setOnMarkerClickListener { marker, _ ->
                                if (!binding.btnSetAsTarget.isChecked) {
                                    viewModel.setSelectedMarker(marker)
                                }
                                true
                            }
                        }
                        markersList.add(temp)
                        map.overlays.add(temp)

                    }
                }
            }
        }

        // Observe selected point
        viewModel.selectedMarker.observe(viewLifecycleOwner) {
            if (it != null) {
                map.overlays.add(clearSelectedMarkerOverlay)
                binding.selectedMarkerDialog.visibility = View.VISIBLE
                var title = it.title
                if (title.length > 30)
                    title = title.substring(0, 30) + "..."
                binding.titleInputField.setText(title)
                map.controller.animateTo(it.position)
            } else {
                binding.selectedMarkerDialog.visibility = View.GONE
                map.overlays.remove(clearSelectedMarkerOverlay)
            }
        }
        viewModel.distanceToTargetString.observe(viewLifecycleOwner) {
            it?.let { distanceString ->
                binding.distanceInputField.setText(distanceString)
            } ?: binding.distanceInputField.setText(getString(R.string.unknown))
        }

        viewModel.elevationString.observe(viewLifecycleOwner) {
            it?.let { elevationString ->
                binding.elevationInputField.setText(elevationString)
            } ?: binding.elevationInputField.setText(getString(R.string.unknown))
        }

        viewModel.trekLocations.observe(viewLifecycleOwner) { liveData ->
            if (liveData != null) {
                liveData.observe(viewLifecycleOwner) { trek ->
                    val temp = trek
                    temp?.let {
                        it.trekList.forEach { innerlist ->
                            val poly = Polyline().apply {
                                color = Color.RED
                            }
                            poly.setPoints(innerlist)
                            trekOverlay.add(poly)
                        }
                        map.overlays.addAll(trekOverlay)
                        map.invalidate()
                    }
                }
            } else {
                map.overlays.removeAll(trekOverlay)
                trekOverlay.clear()
                map.invalidate()
            }
        }

        binding.btnSetAsTarget.addOnCheckedChangeListener { _, isChecked ->
            viewModel.setSelectedAsTargetMarker(isChecked)
        }

        // Observe if we have a target location
        viewModel.selectedMarkerIsTarget.observe(viewLifecycleOwner) {
            if (it == true) {
                map.overlays.remove(clearSelectedMarkerOverlay)
            } else if (viewModel.selectedMarker.value != null) {
                map.overlays.add(clearSelectedMarkerOverlay)
            }
        }
        // TODO This can be done better with a simple point.
        //  Especially to put it on the bottom of overlays
        viewModel.pathPointsToTarget.observe(viewLifecycleOwner) {
            pathToTarget.setPoints(it)
            map.invalidate()

        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it != null)
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        binding.btnGoToMyPointPage.addOnCheckedChangeListener { button, isChecked ->
            when (isChecked) {
                true -> button.isChecked = false
                else -> {}
            }
        }

        binding.btnGoToMyPointPage.apply {
            setOnClickListener {
                viewModel.selectedMarker.value?.id?.let {
                    findNavController().navigate(
                        TrackingFragmentDirections.actionTrackingFragmentToPointOfInterestFragment(
                            it.toLong()
                        )
                    )
                }
            }
        }





        return binding.root
    }


    override fun onPause() {
        super.onPause()
        map.onPause()
        orientationProvider.stopOrientationProvider()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        viewModel.currentPosition.value?.let {
            map.controller.animateTo(it)

            geoField = GeomagneticField(
                it.latitude.toFloat(),
                it.longitude.toFloat(),
                it.altitude.toFloat(),
                System.currentTimeMillis()
            )

            declination = geoField.declination //deviation from true north
        }

        val sharedPrefs = activity?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val limit = sharedPrefs?.getInt("limit", 5)
        viewModel.refreshList(limit!!)
        orientationProvider.startOrientationProvider { orientation, source ->
            //Log.d("Declination",declination.toString())
            clMark.rotation = -orientation + declination
            map.invalidate()
        }
        viewModel.currentPosition.observe(viewLifecycleOwner) { curPos ->
            clMark.position = curPos
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            context?.startService(this)
        }
        if (::map.isInitialized) {
            map.onDetach()
        }
    }


    private fun requestPermissions() {
        if (PermissionCheckUtility.hasAllPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permissions_rationale),
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permissions_rationale),
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun setUpBottomNavTrackingFragmentButtons() {
        binding.btnTrack.apply {
            this.addOnCheckedChangeListener { button, isChecked ->
                when (isChecked) {
                    true -> button.isChecked = false
                    false -> {}
                }
            }
            this.setOnClickListener {
                findNavController().navigate(TrackingFragmentDirections.actionTrackingFragmentToNowTrackingFragment())
            }
        }
        binding.btnGoToMyLocation.apply {
            this.addOnCheckedChangeListener { button, isChecked ->
                when (isChecked) {
                    true -> button.isChecked = false
                    false -> {}
                }
            }
            this.setOnClickListener {
                viewModel.currentPosition.value?.let {
                    map.controller.animateTo(it)
                }
            }
        }
        binding.btnAddPoint.addOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.add_poi_help), Toast.LENGTH_SHORT
                ).show()
            }
            viewModel.setAddingCustomMarker(isChecked)
        }

        binding.btnViewInArMode.setOnClickListener {
            val markedGeoPoint = viewModel.selectedMarker.value?.position

            if (markedGeoPoint != null) {
                if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
                    findNavController().navigate(
                        TrackingFragmentDirections.actionTrackingFragmentToArFragment(
                            markedGeoPoint
                        )
                    )
                } else {
                    //TODO replace this with alertdialog that takes you to permissions page
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.missing_camera_permissions),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.go_to_ar_help), Toast.LENGTH_SHORT
                ).show()

            }
        }

    }

    // This works with the MapEventsOverlay to add clicklisteners and lets us add POIs
    private fun getEventsReceiver(): MapEventsReceiver {
        return object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                if (map.isLayoutOccurred && viewModel.addingCustomMarker.value == true) {
                    viewModel.setAddingCustomMarker(false)
                    binding.btnAddPoint.isChecked = false
                    findNavController().navigate(
                        TrackingFragmentDirections.actionTrackingFragmentToSaveMyPointFragment(
                            TYPE_POI,
                            geoPoint,
                            null
                        )
                    )
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
    }

    private fun getClearSelectedMarkerEventsReceiver(): MapEventsReceiver {
        return object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                if (!binding.btnSetAsTarget.isChecked) {
                    val temp = map.mapCenter
                    viewModel.clearSelectedMarker()
                    map.controller.setCenter(temp)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
    }

}