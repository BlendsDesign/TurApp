package com.example.turapp.viewmodels

import android.app.Application
import android.graphics.Color
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
import org.osmdroid.views.overlay.Polyline
import java.sql.Time


class TrackingViewModel(private val app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)

    private var locationClient = DefaultLocationClient(
        app.applicationContext,
        LocationServices.getFusedLocationProviderClient(app.applicationContext)
    )


    private val _myPointList = MutableLiveData<List<MyPointWithGeo>>()
    val myPointList: LiveData<List<MyPointWithGeo>> get() = _myPointList

    private val _selectedMarker = MutableLiveData<Marker?>()
    val selectedMarker: LiveData<Marker?> get() = _selectedMarker
    private val _selectedMarkerIsTarget = MutableLiveData<Boolean>()
    val selectedMarkerIsTarget: LiveData<Boolean> get() = _selectedMarkerIsTarget
    private val _distanceToTargetString = MutableLiveData<String?>()
    val distanceToTargetString: LiveData<String?> get() = _distanceToTargetString
    private val _pathPointsToTarget = MutableLiveData<MutableList<GeoPoint>>()
    val pathPointsToTarget : LiveData<MutableList<GeoPoint>> get() = _pathPointsToTarget
    fun updatePathPointsToTarget() {
        val list = mutableListOf<GeoPoint>()
        _currentPosition.value?.let {
            list.add(it)
        }
        _selectedMarker.value?.let {
            list.add(it.position)
        }
        if(list.size > 1)
            _pathPointsToTarget.value = list
        else
            _pathPointsToTarget.value = mutableListOf()
    }
    fun setSelectedMarker(marker: Marker) {
        if (_selectedMarkerIsTarget.value != true) {
            marker.icon =
                AppCompatResources.getDrawable(app.applicationContext, R.drawable.ic_marker_blue)
            _selectedMarker.value = marker
            _currentPosition.value?.let {
                setDistanceToTargetString(it)
            }
        }
    }
    fun setSelectedAsTargetMarker(isChecked: Boolean) {

        _selectedMarkerIsTarget.value = isChecked
        if (!isChecked)
            _pathPointsToTarget.value = mutableListOf()
        _selectedMarker.value?.let {
            if (isChecked) {
                it.icon = AppCompatResources.getDrawable(
                    app.applicationContext,
                    R.drawable.ic_marker_red
                )
                updatePathPointsToTarget()
            } else {
                it.icon = AppCompatResources.getDrawable(
                    app.applicationContext,
                    R.drawable.ic_marker_blue
                )
            }
        }
    }

    fun clearSelectedMarker() {
        if (_selectedMarker.value != null) {
            _selectedMarker.value?.icon = AppCompatResources.getDrawable(
                app.applicationContext,
                R.drawable.ic_marker_orange
            )
            _selectedMarker.value = null
            _distanceToTargetString.value = null
            _pathPointsToTarget.value = mutableListOf()
        }
    }

    private fun setDistanceToTargetString(current: GeoPoint) {
        _selectedMarker.value?.let {
            val target: GeoPoint = it.position
            val distance = current.distanceToAsDouble(target)
            _distanceToTargetString.value = String.format("%.2f meters", distance)
        }
    }

    //val locGeoPoint: LiveData<GeoPoint> = locationListener.locGeoPoint

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = _currentLocation

    private val _currentPosition = MutableLiveData<GeoPoint>()
    val currentPosition: LiveData<GeoPoint> get() = _currentPosition

    private val _startingPoint = MutableLiveData<GeoPoint>()
    val startingPoint: LiveData<GeoPoint> get() = _startingPoint

    private val _markersForMap = MutableLiveData<MutableList<Marker>>()
    val markersForMap: LiveData<MutableList<Marker>> get() = _markersForMap

    private val _deviceOrientation = MutableLiveData<Float>()
    val deviceOrientation: LiveData<Float> get() = _deviceOrientation

    private val _isTracking = MutableLiveData<Boolean>()
    val isTracking: LiveData<Boolean> get() = _isTracking

    fun switchIsTracking() {
        _isTracking.value = _isTracking.value != true
    }

    private val stepCountSensor = StepDetectorSensor(app)
    private var _stepCountData = MutableLiveData<Float>()
    val stepCountData: LiveData<Float> get() = _stepCountData

    private val _bearing = MutableLiveData<Float>()
    val bearing: LiveData<Float> get() = _bearing

    private val _bearingAccuracy = MutableLiveData<Float>()
    val bearingAccuracy: LiveData<Float> get() = _bearingAccuracy

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
                val geo = GeoPoint(lat, long, alt)
                _bearingAccuracy.value = location.accuracy
                _bearing.value = location.bearing
                _currentPosition.value = geo
                if (_selectedMarker.value != null) {
                    setDistanceToTargetString(geo)
                    if (_selectedMarkerIsTarget.value == true)
                        updatePathPointsToTarget()
                }

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