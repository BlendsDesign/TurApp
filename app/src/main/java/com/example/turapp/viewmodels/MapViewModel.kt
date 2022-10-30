package com.example.turapp.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.example.turapp.utils.Sensors.StepCounterSensor
import com.example.turapp.repository.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PointOfInterest
import com.example.turapp.roomDb.entities.RecordedActivity
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.*


class MapViewModel(private val app: Application) : ViewModel(), LocationListener {


    // Setting up LOCATION SERVICE
    private val lm: LocationManager = app
        .getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun setUpLocationUpdates() {
        if (checkPermissions()) {
            lm.requestLocationUpdates(GPS_PROVIDER, 1000, 0f, this)
        }
    }

    // Setting up STEP COUNTER
    private var _startingSteps: Float = 0f
    private val _stepCounterData = MutableLiveData<Float>()
    private val stepCounterSensor = StepCounterSensor(app)
    val stepCounterData: LiveData<Float> get() = _stepCounterData
    fun clearStepCount() {
        _startingSteps = _stepCounterData.value ?: 0f
    }

    // Livedata for starting position, current position and position information
    private val _currentPos = MutableLiveData<GeoPoint>()
    val currentPos: LiveData<GeoPoint> get() = _currentPos
    private val _startingPos = MutableLiveData<GeoPoint>()
    val startingPos: LiveData<GeoPoint> get() = _startingPos
    private val _positionInformation = MutableLiveData<String>()
    val positionInformation: LiveData<String> get() = _positionInformation
    private val _selectedMarker = MutableLiveData<Marker>()
    val selectedMarker: LiveData<Marker> get() = _selectedMarker
    fun setSelectedPoiGeoPoint(p: Marker) {
        _selectedMarker.value = p
    }

    private val repository = MyRepository(PoiDatabase.getInstance(app.applicationContext).poiDao)

    private var _pointsOfInterest = MutableLiveData<List<PointOfInterest>>()
    val pointsOfInterest: LiveData<List<PointOfInterest>> get() = _pointsOfInterest

    // Helpers to add a POI
    private var _addingPOI = MutableLiveData<GeoPoint?>()
    val addingPOI: LiveData<GeoPoint?> get() = _addingPOI
    fun setAddingPOI(point: GeoPoint?) {
        _addingPOI.value = point
    }

    fun addPoiCancel() {
        _addingPOI.value = null
    }

    fun addPoi(title: String, desc: String) {
        viewModelScope.launch {
            val geo = _addingPOI.value
            if (geo != null) {
                val poi = PointOfInterest(
                    poiName = title,
                    poiDescription = desc,
                    poiLat = geo.latitude.toFloat(),
                    poiLng = geo.longitude.toFloat(),
                    poiAltitude = geo.altitude.toFloat()
                )
                repository.addSinglePoi(poi)
                refreshPointOfInterest()
            }
            _addingPOI.value = null
        }
    }


    private val _trackedLocations = MutableLiveData<MutableList<Location>>()
    val trackedLocations: LiveData<MutableList<Location>> get() = _trackedLocations

    private val _tracking = MutableLiveData<Boolean>()
    val tracking: LiveData<Boolean> get() = _tracking

    // PAUSE IS NOT REALLY IMPLEMENTED
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

    fun saveTrackedLocations(title: String, desc: String) {
        viewModelScope.launch {
            val geoList: List<Location> = _trackedLocations.value ?: listOf()
            _trackedLocations.value = mutableListOf()
            if (geoList.isNotEmpty()) {
                val timeTaken: Long = geoList[geoList.size - 1].time - geoList[0].time
                var distance = 0.0
                var temp = geoList[0]
                geoList.forEach {
                    distance += it.distanceTo(temp)
                    temp = it
                }
                val ra = RecordedActivity(
                    title = title, description = desc, timestamp = geoList.first().time,
                    timeInMillis = timeTaken, totalDistance = distance.toInt(),
                    startingLat = geoList.first().latitude.toFloat(),
                    startingLng = geoList.first().longitude.toFloat(),
                    startingAltitude = geoList.first().altitude.toFloat()
                )
                repository.insertRecordedActivityAndGeoData(ra, geoList)
            }
        }
    }


    private val _editPointOfInterest = MutableLiveData<Boolean>()
    val editPointOfInterest: LiveData<Boolean> get() = _editPointOfInterest
    fun switchEditPointOfInterest() {
        _editPointOfInterest.value = _editPointOfInterest.value != true
        refreshPointOfInterest()
    }


    init {
        _trackedLocations.value = mutableListOf<Location>()
        _tracking.value = false
        _editPointOfInterest.value = false
        refreshPointOfInterest()
        setUpLocationUpdates()
        stepCounterSensor.startListening()
        stepCounterSensor.setOnSensorValuesChangedListener {
            if (_stepCounterData.value == null) {
                _startingSteps = it[0]
            }
            _stepCounterData.value = it[0] - _startingSteps
        }
    }

    fun refreshPointOfInterest() {
        viewModelScope.launch {
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
            val temp: MutableList<Location> = _trackedLocations.value ?: mutableListOf()
            temp.add(loc)
            _trackedLocations.value = temp
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