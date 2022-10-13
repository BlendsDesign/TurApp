package com.example.turapp.mapView

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.BuildConfig
import com.example.turapp.Helper
import com.example.turapp.R
import com.example.turapp.databinding.FragmentMapBinding
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import java.io.IOException
import java.util.*


class MapFragment : Fragment(), LocationListener {

    private lateinit var binding : FragmentMapBinding

    private val REQUEST_CODE = 123
    private lateinit var lm: LocationManager
    var locLL: LatLng? = null

    private lateinit var map : MapView // 3

    private val trackedPath = mutableListOf<GeoPoint>()

    private val ourPOI = mutableListOf<GeoPoint>()

    var startPosition = false //3
    private var tracking = false //3

    private val viewModel: MapViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, MapViewModel.Factory(app))[MapViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.suggestedFix(contextWrapper = ContextWrapper(context))
        binding = FragmentMapBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setUpMapFragmentBottomNav()
        map = binding.mvMap

        lm = requireNotNull(context).getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!checkPermissions()) {
            Log.d("MainActivity", "Asking for permissions")
            requestAllPermission()
        } else {
            Log.d("MainActivity", "Permissions already granted, started location updates")
            Toast.makeText(context, "Starting location updates", Toast.LENGTH_SHORT).show()
            lm.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,  //GPS_PROVIDER,
                1000, 0f, this
            )
        }

        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
        map.setMultiTouchControls(true) //3

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val compass = CompassOverlay(
            context,
            InternalCompassOrientationProvider(context), map
        )
        compass.enableCompass()
        map.overlays.add(compass)

        viewModel.pointsOfInterest.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                map.overlays.clear()
                map.overlays.add(compass)
                map.overlays.add(MapEventsOverlay(getEventsReceiver()))
                it.forEach { poi ->
                    if (poi.poiLat != null && poi.poiLng != null) {
                        val posMarker = Marker(map)
                        if(poi.poiAltitude != null) {
                            posMarker.position = GeoPoint(
                                poi.poiLat!!.toDouble(),
                                poi.poiLng!!.toDouble(),
                                poi.poiAltitude!!.toDouble()
                            )
                        } else {
                            posMarker.position = GeoPoint(
                                poi.poiLat!!.toDouble(),
                                poi.poiLng!!.toDouble()
                            )
                        }
                        posMarker.apply{
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = poi.poiName
                            subDescription = poi.poiDescription
                        }
                        map.overlays.add(posMarker)
                    }
                }


            }
        })

        viewModel.trackedLocations.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                TODO("Still needs to be implemented")
            }
        })

        map.overlays.add(MapEventsOverlay(getEventsReceiver()))

        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun requestAllPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
            ), REQUEST_CODE
        ) //2
    }

    private fun getEventsReceiver(): MapEventsReceiver {
        return object: MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                viewModel.addPointOfInterest(p)
//                    val selectedPosMarker = Marker(map)
//                    selectedPosMarker.position = p
//                    selectedPosMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                    selectedPosMarker.title = "My Selected Point ${
//                        getLocationInformation(
//                            LatLng(
//                                p.latitude,
//                                p.longitude
//                            )
//                        )
//                    }"
//                    selectedPosMarker.subDescription = "Testing if user can select a point"
//                    val test = PointOfInterest(poiName = "Hello")
//                    map.overlays.add(selectedPosMarker)
                    return false
                }
        }
    }


    private fun checkPermissions(): Boolean {
        return (
                ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED
                )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MapFragment", "Permissions granted, started location updates")
                    Toast.makeText(context, "Starting location updates", Toast.LENGTH_SHORT).show()
                    lm.requestLocationUpdates(
                        LocationManager.FUSED_PROVIDER,
                        1000, 0F, this
                    )
                } else {
                    Toast.makeText(
                        context,
                        "Permissions not granted, degraded version available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    override fun onLocationChanged(location: Location) {
        viewModel.addRoutePoint(location)
        locLL = LatLng(location.latitude, location.longitude)
        Log.d("MapFragment", location.accuracy.toString())
        Log.d("MapFragment", getLocationInformation(locLL!!)!!)
        binding.tvMapViewTop.text = getLocationInformation(locLL!!)
        if (!startPosition /*&& location.getAccuracy() < 15*/) {
            //my current location
            val startPoint = GeoPoint(locLL!!.latitude, locLL!!.longitude)
            Log.d("MapFragment", startPoint.toString())

            startPosition = true
            tracking = true
            val startPosMarker = Marker(map)
            startPosMarker.position = startPoint
            startPosMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            startPosMarker.title = "My Starting position"
            startPosMarker.subDescription = "We are starting our hike from here"
            map.overlays.add(startPosMarker) //a Marker is an overlay
            trackedPath.add(startPoint)
            map.controller.apply {
                setZoom(20.0)
                setCenter(startPoint)
            }
        }
        if (tracking) {
            val currPos = GeoPoint(locLL!!.latitude, locLL!!.longitude)
            trackedPath.add(currPos)
            val path = Polyline()
            path.color = Color.RED
            path.setPoints(trackedPath)
            map.overlayManager.add(path)
            map.invalidate() //make sure the map is redrawn
            map.controller.apply {
                setCenter(currPos)
            }
        }
    }

//    private void analyzePath(List<GeoPoint> trackedPath)
//    {
//        int i,j,k;
//        //loop points
//        GeoPoint curr = trackedPath.get(i);
//        GeoPoint next = trackedPath.get(j);
//        GeoPoint next_next = trackedPath.get(k);
//
//        if(!distanceBetween(curr,next) < 10.0
//        && !distanceBetween(next,next_next) < 10.0) //length between two geolocations
//        {
//
//        }
//
//
//
//    }

    private fun distanceBetween(curr: GeoPoint, next: GeoPoint): Double {
        //potentially use Helper for storage
        return curr.distanceToAsDouble(next)
    }

    //    private void analyzePath(List<GeoPoint> trackedPath)
    //    {
    //        int i,j,k;
    //        //loop points
    //        GeoPoint curr = trackedPath.get(i);
    //        GeoPoint next = trackedPath.get(j);
    //        GeoPoint next_next = trackedPath.get(k);
    //
    //        if(!distanceBetween(curr,next) < 10.0
    //        && !distanceBetween(next,next_next) < 10.0) //length between two geolocations
    //        {
    //
    //        }
    //
    //
    //
    //    }
    //    private boolean distanceBetween(GeoPoint curr, GeoPoint next) {
    //        //potentially use Helper for storage
    //        return curr - next;
    //    }
    private fun getLocationInformation(locLL: LatLng): String? {
        val gc = Geocoder(requireContext(), Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(locLL.latitude, locLL.longitude, 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { //2
    }

    private fun setUpMapFragmentBottomNav() {
        binding.bottomNavMapFragment.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.miStartTracking -> {
                    viewModel.switchTracking()
                    binding.bottomNavMapFragment.apply{
                        this.menu.clear()
                        this.inflateMenu(R.menu.while_tracking_bottom_nav)
                    }
                }
                R.id.miPauseTracking -> {
                    if (viewModel.tracking.value == true) {
                        it.setIcon(R.drawable.ic_play_arrow)
                        it.title = getString(R.string.resume_tracking)
                        viewModel.apply{
                            switchPaused()
                        }
                    } else {
                        it.setIcon(R.drawable.ic_pause)
                        it.title = getString(R.string.pause_run)
                        viewModel.apply{
                            switchPaused()
                        }
                    }
                }
                R.id.endTracking -> {
                    Toast.makeText(context,
                        "Implement a Save run function in fun setUpMapFragmentBottomNav",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.switchTracking()
                    binding.bottomNavMapFragment.apply {
                        this.menu.clear()
                        this.inflateMenu(R.menu.map_fragment_bottom_nav)
                    }
                }
                R.id.miEditPoint -> {
                    if (viewModel.editPointOfInterest.value != true) {
                        it.setIcon(R.drawable.ic_edit_off_24)
                        it.title = "Done editing"
                    } else {
                        it.setIcon(R.drawable.ic_edit_24)
                        it.title = getString(R.string.edit_pois)
                    }
                    Toast.makeText(context,
                        "Implement a Save changes to poi function in fun setUpMapFragmentBottomNav",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.switchEditPointOfInterest()
                }
            }
            true
        }
    }
}