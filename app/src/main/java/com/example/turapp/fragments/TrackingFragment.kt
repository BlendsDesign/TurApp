package com.example.turapp.fragments

import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.utils.helperFiles.Helper
import com.example.turapp.R
import com.example.turapp.databinding.FragmentTrackingBinding
import com.example.turapp.utils.helperFiles.REQUEST_CODE_LOCATION_PERMISSION
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.viewmodels.TrackingViewModel
import com.example.turapp.utils.locationClient.LocationService
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Marker.OnMarkerDragListener
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*


class TrackingFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var clearSelectedMarkerOverlay: MapEventsOverlay = MapEventsOverlay(getClearSelectedMarkerEventsReceiver())

    private lateinit var binding: FragmentTrackingBinding

    private val markersList = mutableListOf<Marker>()

    private lateinit var map: MapView // 3

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

        val mOrientationProvider = SensorManager.SENSOR_ORIENTATION

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

        binding.fabTrackingHelp.setOnClickListener {
            //TODO Add an alertdialog for this
        }
        lifecycleScope.launchWhenCreated {
            map = binding.trackingMap
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
            map.setMultiTouchControls(true) //3
            map.setTileSource(TileSourceFactory.MAPNIK)
            InternalCompassOrientationProvider(requireContext()).startOrientationProvider { orientation, source ->
                map.mapOrientation = orientation
            }
            val compass = CompassOverlay(
                context,
                InternalCompassOrientationProvider(context), map
            )
            compass.enableCompass()

            map.overlays.add(compass)
            map.controller.setZoom(18.0)
            viewModel.startingPoint.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    map.controller.animateTo(it)
                    map.invalidate()
                }
            })

            val clMarker = Marker(map)
            clMarker.icon = getDrawable(requireContext(), R.drawable.ic_my_location)
            viewModel.currentPosition.observe(viewLifecycleOwner, Observer {
                clMarker.position = it
            })
            map.overlayManager.add(clMarker)
            val myMapEventsOverlay: MapEventsOverlay = MapEventsOverlay(getEventsReceiver())
            map.overlays.add(myMapEventsOverlay)
        }

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

        setUpBottomNavTrackingFragmentButtons()

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
            if(it != null) {
                map.overlays.add(clearSelectedMarkerOverlay)
                binding.selectedMarkerDialog.visibility = View.VISIBLE
                binding.titleInputField.setText(it.title)
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var testOverLay = DirectedLocationOverlay(requireContext())
        testOverLay.setShowAccuracy(true)
        map.overlays.add(testOverLay)
        viewModel.bearing.observe(viewLifecycleOwner, Observer {
            testOverLay.setBearing(it)
            viewModel.bearingAccuracy.value?.toInt()?.let { it1 -> testOverLay.setAccuracy(it1) }
            testOverLay.location = viewModel.currentPosition.value
        })


        // THIS IS JUST TO TEST THE TRACKING SERVICE
        binding.tvForTestsDELETEME.apply {
            text = "PRESS HERE TO TEST LOCATIONSERVICE"
            setOnClickListener {
                if (viewModel.isTracking.value != true) {
                    // Her starter vi Locationservice som skal brukes til tracking
                    Intent(context, LocationService::class.java).apply {
                        action = LocationService.ACTION_START
                        context.startService(this)
                    }
                    viewModel.switchIsTracking()
                } else {
                    Intent(context, LocationService::class.java).apply {
                        action = LocationService.ACTION_STOP
                        context.startService(this)
                    }
                    viewModel.switchIsTracking()
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        viewModel.refreshList()
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
        binding.bottomNavTrackingFragment.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.miStartTracking -> {

                }
                R.id.miGoToMyLocation -> {
                    if (viewModel.currentPosition.value != null) {
                        map.controller.animateTo(viewModel.currentPosition.value)

                    }
                    return@setOnItemSelectedListener false
                }
                R.id.miAddPoint -> {
                    if (viewModel.addingCustomMarker.value == true) {
                        viewModel.setAddingCustomMarker()
                        return@setOnItemSelectedListener false
                    }
                    Toast.makeText(
                        requireContext(), "Click on map to add a point there",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.setAddingCustomMarker()
                }

            }
            false
        }
    }

    // This works with the MapEventsOverlay to add clicklisteners and lets us add POIs
    private fun getEventsReceiver(): MapEventsReceiver {
        return object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                if (viewModel.addingCustomMarker.value == true) {
                    viewModel.setAddingCustomMarker()
                    findNavController().navigate(
                        TrackingFragmentDirections.actionTrackingFragmentToSaveMyPointFragment(
                            geoPoint
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

    private fun getLocationInformation(lat: Double, lng: Double): String? {
        val gc = Geocoder(requireContext(), Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(lat, lng, 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

}