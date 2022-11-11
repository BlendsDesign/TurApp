package com.example.turapp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentTrackingBinding
import com.example.turapp.utils.helperFiles.REQUEST_CODE_LOCATION_PERMISSION
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.viewmodels.TrackingViewModel
import com.example.turapp.utils.locationClient.LocationService
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

    private lateinit var binding: FragmentTrackingBinding

    private val markersList = mutableListOf<Marker>()

    private lateinit var orientationProvider: InternalCompassOrientationProvider

    private lateinit var map: MapView // 3

    private val pathToTarget = Polyline().apply {
        color = Color.CYAN
    }

    private val clMark: Marker by lazy {
        Marker(map).apply {
            icon = getDrawable(requireContext(), R.drawable.ic_my_location_arrow)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }

    private var totalSteps = 0 //
    private var previousTotalSteps = 0f

    private val viewModel: TrackingViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, TrackingViewModel.Factory(app))[TrackingViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        if (PermissionCheckUtility.hasLocationPermissions(requireContext())) {
            viewModel.startLocationClient()
        }

        if (PermissionCheckUtility.hasActivityRecognitionPermissions(requireContext())) {
            loadStepData()
        }

        orientationProvider = InternalCompassOrientationProvider(requireContext())

    }

    private fun saveStepData() {

        val sharedPreferences = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putFloat("key1", previousTotalSteps)
        editor?.apply()
    }

    private fun loadStepData() {
        val sharedPreferences = context?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences?.getFloat("key1", 0f)
        Log.d("loadStepData fun", "$savedNumber")
        if (savedNumber != null) {
            previousTotalSteps = savedNumber
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        setUpBottomNavTrackingFragmentButtons()
        binding.fabTrackingHelp.setOnClickListener {
            //TODO Add an alertdialog for this
        }

        lifecycleScope.launchWhenCreated {
            map = binding.trackingMap
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
            map.setMultiTouchControls(true) //3
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.controller.setZoom(18.0)
            viewModel.startingPoint.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    map.controller.animateTo(it)
                    map.invalidate()
                }
            })

            viewModel.currentPosition.observe(viewLifecycleOwner, Observer { curPos ->
                clMark.position = curPos
            })
            val myMapEventsOverlay = MapEventsOverlay(getEventsReceiver())
            map.overlays.add(myMapEventsOverlay)
        }
        map.overlayManager.add(clMark)

//        viewModel.stepCountData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            totalSteps++
//            binding.tvStepCount.text = ("$totalSteps")
//
//            tvStepCount.setOnClickListener { view ->
//                Toast.makeText(context, "Long tap to reset steps!", Toast.LENGTH_SHORT).show()
//            }
//
//            tvStepCount.setOnLongClickListener { view ->
//                previousTotalSteps = totalSteps.toFloat()
//                totalSteps = 0 //reset
//                binding.tvStepCount.text = 0.toString()
//                saveStepData()
//                true
//            }
//        })

        viewModel.myPointList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                lifecycleScope.launch {
                    // REMOVE EXISTING MARKERS
                    markersList.forEach { marker ->
                        map.overlays.remove(marker)
                    }
                    markersList.clear()

                    it.forEach { point ->
                        if (point.geoData.isNotEmpty()) {
                            val temp = Marker(map)
                            temp.apply {
                                position = point.geoData.first().geoPoint
                                title = point.point.title
                                subDescription = point.point.description
                                icon = getDrawable(requireContext(), R.drawable.ic_marker_orange)
                                id = point.point.pointId.toString()
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
        })

        // Observe selected point
        viewModel.selectedMarker.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                map.overlays.add(clearSelectedMarkerOverlay)
                binding.selectedMarkerDialog.visibility = View.VISIBLE
                var title = it.title
                if (title.length > 30)
                    title = title.substring(0, 30) + "..."
                binding.titleInputField.setText(title)
            } else {
                binding.selectedMarkerDialog.visibility = View.GONE
                map.overlays.remove(clearSelectedMarkerOverlay)
            }
        })
        viewModel.distanceToTargetString.observe(viewLifecycleOwner, Observer {
            it?.let { distanceString ->
                binding.distanceInputField.setText(distanceString)
            } ?: binding.distanceInputField.setText("Unknown")
        })
        binding.btnSetAsTarget.addOnCheckedChangeListener { _, isChecked ->
            viewModel.setSelectedAsTargetMarker(isChecked)
        }

        // Observe if we have a target location
        viewModel.selectedMarkerIsTarget.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                map.overlays.remove(clearSelectedMarkerOverlay)
            } else if (viewModel.selectedMarker.value != null) {
                map.overlays.add(clearSelectedMarkerOverlay)
            }
        })
        viewModel.pathPointsToTarget.observe(viewLifecycleOwner, Observer {
            if (it.size > 1) {
                pathToTarget.setPoints(it)
                map.overlays.add(pathToTarget)
            } else {
                map.overlays.remove(pathToTarget)
                map.invalidate()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        var testOverLay = DirectedLocationOverlay(requireContext())
//        testOverLay.setShowAccuracy(true)
//        map.overlays.add(testOverLay)
//        viewModel.bearing.observe(viewLifecycleOwner, Observer {
//            testOverLay.setBearing(it)
//            viewModel.bearingAccuracy.value?.toInt()?.let { it1 -> testOverLay.setAccuracy(it1) }
//            testOverLay.location = viewModel.currentPosition.value
//        })


    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        orientationProvider.stopOrientationProvider()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        viewModel.refreshList()
        orientationProvider.startOrientationProvider { orientation, source ->
            clMark.rotation = -orientation
            map.invalidate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            context?.startService(this)
        }
        map.onDetach()
    }


    private fun requestPermissions() {
        if (PermissionCheckUtility.hasLocationPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app.",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
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
                    "Click on map to add a point there", Toast.LENGTH_SHORT
                ).show()
            }
            viewModel.setAddingCustomMarker()
        }

    }
//        binding.bottomNavTrackingFragment.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.miStartTracking -> {
//                    findNavController().navigate(TrackingFragmentDirections.actionTrackingFragmentToNowTrackingFragment())
//                }
//                R.id.miGoToMyLocation -> {
//                    if (viewModel.currentPosition.value != null) {
//                        map.controller.animateTo(viewModel.currentPosition.value)
//
//                    }
//                    return@setOnItemSelectedListener false
//                }
//                R.id.miAddPoint -> {
//                    if (viewModel.addingCustomMarker.value == true) {
//                        viewModel.setAddingCustomMarker()
//                        return@setOnItemSelectedListener false
//                    }
//                    Toast.makeText(
//                        requireContext(), "Click on map to add a point there",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    viewModel.setAddingCustomMarker()
//                }
//
//            }
//            false
//        }
//    }

    // This works with the MapEventsOverlay to add clicklisteners and lets us add POIs
    private fun getEventsReceiver(): MapEventsReceiver {
        return object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                if (viewModel.addingCustomMarker.value == true) {
                    viewModel.setAddingCustomMarker()
                    findNavController().navigate(
                        TrackingFragmentDirections.actionTrackingFragmentToSaveMyPointFragment(
                            geoPoint, null
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
                if (!binding.btnSetAsTarget.isChecked)
                    viewModel.clearSelectedMarker()
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
    }
}