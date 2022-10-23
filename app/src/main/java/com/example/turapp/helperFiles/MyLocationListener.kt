package com.example.turapp.helperFiles

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Criteria
import android.location.Criteria.ACCURACY_HIGH
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.FUSED_PROVIDER
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.osmdroid.util.GeoPoint

@SuppressLint("MissingPermission")
class MyLocationListener(private val app: Application): LocationListener {
    private val lm: LocationManager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val criteria = Criteria()

    private var bestProvider: String? = null

    private val _locGeoPoint = MutableLiveData<GeoPoint>()
    val locGeoPoint : LiveData<GeoPoint> get() = _locGeoPoint

    private val _location = MutableLiveData<Location>()
    val location : LiveData<Location> get() = _location

    private val _startingLocation = MutableLiveData<GeoPoint>()
    val startingLocation: LiveData<GeoPoint> get() = _startingLocation



    init {
        val criteria = Criteria()

        bestProvider = lm.getBestProvider(criteria, true)

        if (TrackingUtility.hasLocationPermissions(context = app.applicationContext)) {
            //val temp = lm.getCurrentLocation()
            //_startingLocation.value =
        } else {
            Toast.makeText(app.applicationContext,
                "Missing Location Permissions", Toast.LENGTH_LONG).show()
        }
    }




    override fun onLocationChanged(loc: Location) {
        val geoPoint = GeoPoint(loc.latitude, loc.longitude, loc.altitude)
        _locGeoPoint.value = geoPoint
        _location.value = loc
        if (startingLocation.value == null)
            _startingLocation.value = geoPoint
    }
}