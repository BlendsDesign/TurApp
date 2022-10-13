package com.example.turapp.mapView

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.*
import com.example.turapp.roomDb.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PointOfInterest
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(app: Application) : ViewModel() {

    private val repository = MyRepository(PoiDatabase.getInstance(app.applicationContext).poiDao)

    private var _pointsOfInterest = MutableLiveData<List<PointOfInterest>>()
    val pointsOfInterest: LiveData<List<PointOfInterest>> get() = _pointsOfInterest

    private val _trackedLocations = MutableLiveData<MutableList<Location>>()
    val trackedLocations: LiveData<MutableList<Location>> get() = _trackedLocations
    private val _tracking = MutableLiveData<Boolean>()
    val tracking : LiveData<Boolean> get() = _tracking
    private var paused: Boolean = false
    private var pauseStartTime: Long? = null
    fun switchTracking() {
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
    }

    private val _recordingActivity = MutableLiveData<Boolean>()
    val recordingActivity : LiveData<Boolean> get() = _recordingActivity


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
        if (_recordingActivity.value == true) {
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

    fun savePointsOnRoute() {

        // Empty list last
        _trackedLocations.value = mutableListOf()
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