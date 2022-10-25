package com.example.turapp.viewmodels

import android.app.Application
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.*
import com.example.turapp.utils.locationClient.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker



class TrackingViewModel(private val app: Application): ViewModel() {

    private var locationClient = DefaultLocationClient(
        app.applicationContext,
        LocationServices.getFusedLocationProviderClient(app.applicationContext)
    )

    private val _markersList = MutableLiveData<List<Marker>>()
    val markersList : LiveData<List<Marker>> get() = _markersList

    //val locGeoPoint: LiveData<GeoPoint> = locationListener.locGeoPoint

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = _currentLocation

    private val _currentPosition = MutableLiveData<GeoPoint>()
    val currentPosition: LiveData<GeoPoint> get() = _currentPosition

    private val _startingPoint = MutableLiveData<GeoPoint>()
    val startingPoint : LiveData<GeoPoint> get() = _startingPoint

    private val _markersForMap = MutableLiveData<MutableList<Marker>>()
    val markersForMap : LiveData<MutableList<Marker>> get() = _markersForMap

    private val _isTracking = MutableLiveData<Boolean>()
    val isTracking : LiveData<Boolean> get() = _isTracking
    fun switchIsTracking() {
        _isTracking.value = _isTracking.value != true
    }



    init {

    }
    fun startLocationClient() {
        // This is only for the map, and not for the tracking service
        locationClient.getLocationUpdates(5000L)
            .catch { e -> Toast.makeText(app.applicationContext, e.message, Toast.LENGTH_SHORT).show() }
            .onEach { location ->
                _currentLocation.value = location
                val lat = location.latitude
                val long = location.longitude
                val alt = location.altitude
                val time = location.time
                _currentPosition.value = GeoPoint(lat, long, alt)
                if (_startingPoint.value == null)
                    _startingPoint.value = GeoPoint(lat, long, alt)
            }
            .launchIn(viewModelScope)
    }






    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TrackingViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}