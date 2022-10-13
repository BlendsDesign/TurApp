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

    private val _pointsOfInterest = MutableLiveData<MutableList<PointOfInterest>>()
    val pointsOfInterest: LiveData<MutableList<PointOfInterest>> get() = _pointsOfInterest

    private val _trackedLocations = MutableLiveData<MutableList<Location>>()
    val trackedLocations: LiveData<MutableList<Location>> get() = _trackedLocations
    private val _tracking = MutableLiveData<Boolean>()
    val tracking : LiveData<Boolean> get() = _tracking

    private val _makingPointOfInterest = MutableLiveData<Boolean>()
    val makingPointOfInterest : LiveData<Boolean> get() = _makingPointOfInterest

    private val _recordingActivity = MutableLiveData<Boolean>()
    val recordingActivity : LiveData<Boolean> get() = _recordingActivity

    init {
        _trackedLocations.value = mutableListOf<Location>()
        _tracking.value = false
        _makingPointOfInterest.value = false
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

    fun switchTracking() {
        _tracking.value = _tracking.value != true
    }



    fun addPointOfInterest(point: GeoPoint, title: String? = null, desc: String? = null) {
        viewModelScope.launch {
            repository.addSinglePoi(PointOfInterest(
                poiName= title?: "Unnamed POI",
                poiDescription = desc,
                poiLat = point.latitude.toFloat(),
                poiLng = point.longitude.toFloat()
            ))
        }
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