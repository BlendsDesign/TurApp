package com.example.turapp.viewmodels

import android.app.Application
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.*
import com.example.turapp.R
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import com.example.turapp.utils.MyPointRepository
import com.example.turapp.utils.OSMDroidUtils
import com.example.turapp.utils.Sensors.StepDetectorSensor
import com.example.turapp.utils.locationClient.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.sql.Time


class TrackingViewModel(private val app: Application): ViewModel() {

    private val repository = MyPointRepository(app)

    private var locationClient = DefaultLocationClient(
        app.applicationContext,
        LocationServices.getFusedLocationProviderClient(app.applicationContext)
    )



    private val _myPointList = MutableLiveData<List<MyPointWithGeo>>()
    val myPointList : LiveData<List<MyPointWithGeo>> get() = _myPointList

    private val _selectedMarker = MutableLiveData<Marker?>()
    val selectedMarker : LiveData<Marker?> get() = _selectedMarker
    fun setSelectedMarker(marker: Marker) {
        marker.icon = AppCompatResources.getDrawable(app.applicationContext, R.drawable.ic_marker_blue)
        _selectedMarker.value = marker
    }
    fun clearSelectedMarker() {
        if (_selectedMarker.value != null) {
            _selectedMarker.value?.icon = AppCompatResources.getDrawable(app.applicationContext, R.drawable.ic_marker_orange)
            _selectedMarker.value = null
        }

    }
    private val _targetMarker = MutableLiveData<Marker?>()
    val targetMarker : LiveData<Marker?> get() = _targetMarker
    fun setTargetMarker(marker: Marker) {
        _targetMarker.value = marker
    }
    fun clearTargetMarker() {
        _targetMarker.value = null
    }

    //val locGeoPoint: LiveData<GeoPoint> = locationListener.locGeoPoint

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = _currentLocation

    private val _currentPosition = MutableLiveData<GeoPoint>()
    val currentPosition: LiveData<GeoPoint> get() = _currentPosition

    private val _startingPoint = MutableLiveData<GeoPoint>()
    val startingPoint : LiveData<GeoPoint> get() = _startingPoint

    private val _markersForMap = MutableLiveData<MutableList<Marker>>()
    val markersForMap : LiveData<MutableList<Marker>> get() = _markersForMap

    private val _deviceOrientation = MutableLiveData<Float>()
    val deviceOrientation: LiveData<Float> get() = _deviceOrientation

    private val _isTracking = MutableLiveData<Boolean>()
    val isTracking : LiveData<Boolean> get() = _isTracking

    fun switchIsTracking() {
        _isTracking.value = _isTracking.value != true
    }

    private val stepCountSensor = StepDetectorSensor(app)
    private var _stepCountData = MutableLiveData<Float>()
    val stepCountData: LiveData<Float> get() = _stepCountData

    private val _bearing = MutableLiveData<Float>()
    val bearing : LiveData<Float> get() = _bearing

    private val _bearingAccuracy = MutableLiveData<Float>()
    val bearingAccuracy : LiveData<Float> get() = _bearingAccuracy

    private val _addingCustomMarker = MutableLiveData<Boolean>()
    val addingCustomMarker: LiveData<Boolean> get() = _addingCustomMarker
    fun setAddingCustomMarker() {
        _addingCustomMarker.value = _addingCustomMarker.value != true
    }



    init {
        stepCountSensor.startListening()
        stepCountSensor.setOnSensorValuesChangedListener {
            _stepCountData.value = it[0]
        }
    }

    fun refreshList() {
        viewModelScope.launch {
            _myPointList.value = repository.getAllMyPointsWithGeo()
        }
    }

    fun startLocationClient() {
        // This is only for the map, and not for the tracking service
        locationClient.getLocationUpdates(500L)
            .catch { e -> e.printStackTrace()/*Toast.makeText(app.applicationContext, e.message, Toast.LENGTH_SHORT).show()*/ }
            .onEach { location ->
                _currentLocation.value = location
                val lat = location.latitude
                val long = location.longitude
                val alt = location.altitude
                val time = location.time
                _bearingAccuracy.value = location.accuracy
                _bearing.value = location.bearing
                _currentPosition.value = GeoPoint(lat, long, alt)

                if (_startingPoint.value == null)
                    _startingPoint.value = GeoPoint(lat, long, alt)
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
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