package com.example.turapp.mapView

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.example.turapp.roomDb.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PointOfInterest
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.util.*


class MapViewModel(private val app: Application) : ViewModel(), LocationListener {


    // Setting up LOCATION SERVICE
    private val lm: LocationManager = app.applicationContext
        .getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun setUpLocationUpdates() {
        if (checkPermissions()) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
        }
    }

    // Livedata for starting position, current position and position information
    private val _currentPos = MutableLiveData<GeoPoint>()
    val currentPos: LiveData<GeoPoint> get() = _currentPos
    private val _startingPos = MutableLiveData<GeoPoint>()
    val startingPos : LiveData<GeoPoint> get() = _startingPos
    private val _positionInformation = MutableLiveData<String>()
    val positionInformation : LiveData<String> get() = _positionInformation

    private val repository = MyRepository(PoiDatabase.getInstance(app.applicationContext).poiDao)

    private var _pointsOfInterest = MutableLiveData<List<PointOfInterest>>()
    val pointsOfInterest: LiveData<List<PointOfInterest>> get() = _pointsOfInterest

    private val _trackedLocations = MutableLiveData<MutableList<Location>>()
    val trackedLocations: LiveData<MutableList<Location>> get() = _trackedLocations

    private val _tracking = MutableLiveData<Boolean>()
    val tracking : LiveData<Boolean> get() = _tracking

    private var paused: Boolean = false

    private var pauseStartTime: Long? = null

    fun switchTracking() { // This starts and stops tracking
        _tracking.value = _tracking.value != true
    }
    fun switchPaused() {
        if (_tracking.value == true) {
            pauseStartTime = System.currentTimeMillis()
            paused = true
            _tracking.value = false
        } else paused = false
    }

    private val _editPointOfInterest = MutableLiveData<Boolean>()
    val editPointOfInterest : LiveData<Boolean> get() = _editPointOfInterest
    fun switchEditPointOfInterest(){
        _editPointOfInterest.value = _editPointOfInterest.value != true
        refreshPointOfInterest()
    }


    init {
        _trackedLocations.value = mutableListOf<Location>()
        _tracking.value = false
        _editPointOfInterest.value = false
        refreshPointOfInterest()
    }

    fun refreshPointOfInterest(){
        viewModelScope.launch{
            _pointsOfInterest.value = repository.getAllPoi()
        }
    }


    // Let user set a series of points points
    fun addRoutePoint(point: Location) {
        if (_tracking.value == true) {
            val list: MutableList<Location> =
                _trackedLocations.value ?: mutableListOf()
            list.add(point)
            _trackedLocations.value = list
        }
    }

    fun addPointOfInterest(point: GeoPoint) {
        viewModelScope.launch {
            val poi = PointOfInterest(
                poiName=  "Testing map POI",
                poiDescription = "Still a test",
                poiLat = point.latitude.toFloat(),
                poiLng = point.longitude.toFloat(),
                poiAltitude = point.altitude.toFloat()
            )
            repository.addSinglePoi(poi)
        }
        refreshPointOfInterest()
    }

    fun deletePointOfInterest(poi: PointOfInterest) {
        viewModelScope.launch {
            repository.deletePoiAndRecordings(poi)
            refreshPointOfInterest()
        }
    }

    fun replacePointOfInterest(poi: PointOfInterest) {
        viewModelScope.launch {
            repository.addSinglePoi(poi)
            refreshPointOfInterest()
        }
    }

    fun savePointsOnRoute() {

        // Empty list last
        _trackedLocations.value = mutableListOf()
    }

    override fun onLocationChanged(loc: Location) {
        _currentPos.value = GeoPoint(loc.latitude, loc.longitude, loc.altitude)
        if (_startingPos.value == null) {
            _startingPos.value = GeoPoint(loc.latitude, loc.longitude, loc.altitude)
        }
        if (_tracking.value == true && !paused) {
            _trackedLocations.value!!.add(loc)
        }
        _positionInformation.value = getLocationInformation(loc.latitude, loc.longitude)
    }

    private fun checkPermissions(): Boolean {
        return (
                ActivityCompat.checkSelfPermission(
                    app.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                        &&
                        ActivityCompat.checkSelfPermission(
                            app.applicationContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        &&
                        ActivityCompat.checkSelfPermission(
                            app.applicationContext,
                            Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
                        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun getLocationInformation(lat: Double, lng: Double): String? {
        val gc = Geocoder(app.applicationContext, Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(lat, lng, 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }




    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}