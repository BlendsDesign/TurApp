package com.example.turapp.mapView

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.BuildConfig
import com.example.turapp.Helper
import com.example.turapp.R
import com.example.turapp.databinding.FragmentMapBinding
import com.example.turapp.roomDb.entities.PointOfInterest
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.Distance
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Marker.OnMarkerDragListener
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import java.io.IOException
import java.util.*

private val REQUEST_CODE = 123

class MapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding

    private lateinit var map: MapView // 3


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

        setUpMapFragmentBottomButtons()

        // Setting up text window at top to show adress information as it comes in
        viewModel.positionInformation.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            binding.tvCurrentLocationInformation.text = it
        })

        // Setting up mvMap
        map = binding.mvMap
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
        // This adds an eventOverlay that lets us configure a longpress listener
        map.overlays.add(MapEventsOverlay(getEventsReceiver()))

        // This LiveData is updated when
        // override onLocationChanged in the viewModel gets its first Location
        viewModel.startingPos.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            map.controller.apply {
                setZoom(20.0)
                setCenter(it)
            }
        })

        // Observer to see if there are POIs loaded from the DB
        viewModel.pointsOfInterest.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                map.overlays.clear()
                map.overlays.add(compass)
                populateMapWithPois(it)


            }
        })
        // SHOWS THE EDIT TEXTS FOR A NEW POI
        viewModel.addingPOI.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                binding.bottomNavMapFragment.apply {
                    this.menu.clear()
                    this.inflateMenu(R.menu.when_editing_poi_in_mapview)
                }
                binding.poiEditLayout.visibility = View.VISIBLE
                binding.textViewPoiAdress.text = getLocationInformation(it.latitude, it.longitude)
            } else {
                binding.bottomNavMapFragment.apply {
                    this.menu.clear()
                    this.inflateMenu(R.menu.map_fragment_bottom_nav)
                }
                binding.poiEditLayout.visibility = View.GONE
            }
        })

        viewModel.stepCounterData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            binding.tvStepsensorData.text = "$it"
        })

        // Livedata. List with Locations that is updated when tracking
        viewModel.trackedLocations.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                drawTrackedLocations()
            }
        })

        viewModel.currentPos.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val marker = viewModel.selectedMarker.value
            if (marker != null) {
                val distance = it.distanceToAsDouble(marker.position)
                tvDistanceToPOI.text = String.format("Distance to \"${marker.title}\": %.2f m", distance)
            }
        })

        //


        requestAllPermission()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun populateMapWithPois(list: List<PointOfInterest>) {
        if (viewModel.editPointOfInterest.value != true)
            map.overlays.add(MapEventsOverlay(getEventsReceiver()))

        list.forEach { poi ->
            if (poi.poiLat != null && poi.poiLng != null) {
                val posMarker = Marker(map)
                if (poi.poiAltitude != null) {
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
                posMarker.apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = poi.poiName
                    subDescription = poi.poiDescription
                    posMarker.setOnMarkerClickListener { marker, mapView ->
                        marker.showInfoWindow()
                        viewModel.setSelectedPoiGeoPoint(marker)
                        true
                    }

                }
                if (viewModel.editPointOfInterest.value == true) {
                    posMarker.setOnMarkerClickListener { marker, mapView ->
                        val alertDialog = AlertDialog.Builder(context).create()
                        alertDialog.setTitle(getString(R.string.delete_are_you_sure))
                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Yes"
                        ) { dialog: DialogInterface,
                            _: Int ->
                            viewModel.deletePointOfInterest(poi)
                        }
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEGATIVE,
                            "No"
                        ) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        alertDialog.show()
                        false
                    }
                    posMarker.setOnMarkerDragListener(getMarkerDragListener(poi))
                    posMarker.isDraggable = true
                }
                map.overlays.add(posMarker)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun requestAllPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION
            ), REQUEST_CODE
        ) //2
    }

    // This works with the MapEventsOverlay to add clicklisteners and lets us add POIs
    private fun getEventsReceiver(): MapEventsReceiver {
        return object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                viewModel.setAddingPOI(p)
                return false
            }
        }
    }

    private fun drawTrackedLocations() {

        Log.d("drawTrackedLocations", viewModel.trackedLocations.value!!.lastIndex.toString())
        val it = viewModel.trackedLocations.value ?: mutableListOf()
        if (it.isNotEmpty()) {
            val list = mutableListOf<GeoPoint>()
            it.forEach { loc ->
                list.add(GeoPoint(loc.latitude, loc.longitude, loc.altitude))
            }
            val path = Polyline()
            path.color = Color.RED
            path.setPoints(list)
            map.overlayManager.add(path)
            map.invalidate() //make sure the map is redrawn

        }
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
                    viewModel.setUpLocationUpdates()
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

    fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { //2
    }

    // This is the CONTROL buttons at the bottom of the Fragment. Uses a BottomNavView but does not navigate
    private fun setUpMapFragmentBottomButtons() {
        binding.bottomNavMapFragment.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.miStartTracking -> {
                    viewModel.switchTracking()
                    binding.bottomNavMapFragment.apply {
                        this.menu.clear()
                        this.inflateMenu(R.menu.while_tracking_bottom_nav)
                    }
                }
                R.id.miPauseTracking -> {
                    if (viewModel.tracking.value == true) {
                        it.setIcon(R.drawable.ic_play_arrow)
                        it.title = getString(R.string.resume_tracking)
                        viewModel.apply {
                            switchPaused()
                        }
                    } else {
                        it.setIcon(R.drawable.ic_pause)
                        it.title = getString(R.string.pause_run)
                        viewModel.apply {
                            switchPaused()
                        }
                    }
                }
                R.id.endTracking -> {
                    Toast.makeText(
                        context,
                        "There is a function to save runs, but not to see saved runs\nDue to running out of time",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.saveTrackedLocations(
                        "TEMP ACTIVITY NAME",
                        "Didn't have time to implement function to let user add title and desc fields")
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
                    Toast.makeText(
                        context,
                        "Implement a Save changes to poi function in fun setUpMapFragmentBottomButtons",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.switchEditPointOfInterest()
                }
                R.id.miSaveNewPoi -> {
                    if (viewModel.addingPOI.value != null) {
                        val title = binding.etPoiTitle.text
                        val desc = binding.etPoiDescription.text
                        viewModel.addPoi(title.toString(), desc.toString())
                        //binding.poiEditLayout.visibility = View.GONE
                    }
                    binding.poiEditLayout.visibility = View.GONE
                }
                R.id.miCancelAddingPoi -> {
                    viewModel.addPoiCancel()
                }
            }
            true
        }
    }

    // This allows the user to drag a POI on the map if in edit mode
    private fun getMarkerDragListener(poi: PointOfInterest): OnMarkerDragListener {
        return object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker?) {

            }

            override fun onMarkerDrag(marker: Marker?) {

            }

            override fun onMarkerDragEnd(marker: Marker?) {

                if (marker != null) {
                    val alertDialog = AlertDialog.Builder(context).create()
                    alertDialog.setTitle(getString(R.string.change_poi_location_warning))
                    alertDialog.setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        "Yes"
                    ) { dialog: DialogInterface,
                        _: Int ->
                        poi.poiLat = marker.position.latitude.toFloat()
                        poi.poiLng = marker.position.longitude.toFloat()
                        poi.poiAltitude = marker.position.altitude.toFloat()
                        viewModel.replacePointOfInterest(poi)
                    }
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEGATIVE,
                        "No"
                    ) { dialog: DialogInterface, _: Int ->
                        viewModel.refreshPointOfInterest()
                        dialog.dismiss()
                    }
                    alertDialog.show()
                } else Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }


        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPointOfInterest()
    }
}