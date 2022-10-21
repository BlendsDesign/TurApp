package com.example.turapp.helperFiles

import android.app.Application
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.osmdroid.util.GeoPoint

class MyLocationListener(private val app: Application): LocationListener {


    fun startLocationService(){
    }

    private val _locGeoPoint = MutableLiveData<GeoPoint>()
    val locGeoPoint : LiveData<GeoPoint> get() = _locGeoPoint

    private val _location = MutableLiveData<Location>()
    val location : LiveData<Location> get() = _location

    private val _startingLocation = MutableLiveData<GeoPoint>()
    val startingLocation: LiveData<GeoPoint> get() = _startingLocation




    override fun onLocationChanged(loc: Location) {
        val geoPoint = GeoPoint(loc.latitude, loc.longitude, loc.altitude)
        _locGeoPoint.value = geoPoint
        _location.value = loc
        if (startingLocation.value == null)
            _startingLocation.value = geoPoint
    }
}