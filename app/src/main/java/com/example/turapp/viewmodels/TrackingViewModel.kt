package com.example.turapp.viewmodels

import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.turapp.R
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.MyPointRepository
import com.example.turapp.repository.trackingDb.entities.TrekLocations
import com.example.turapp.utils.locationClient.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.*


class TrackingViewModel(private val app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)

    private var locationClient = DefaultLocationClient(
        app.applicationContext,
        LocationServices.getFusedLocationProviderClient(app.applicationContext)
    )


    private val _myPointList = MutableLiveData<List<MyPoint>>()
    val myPointList: LiveData<List<MyPoint>> get() = _myPointList

    private val _selectedMarker = MutableLiveData<Marker?>()
    val selectedMarker: LiveData<Marker?> get() = _selectedMarker
    private val _selectedMarkerIsTarget = MutableLiveData<Boolean>()
    val selectedMarkerIsTarget: LiveData<Boolean> get() = _selectedMarkerIsTarget
    private val _distanceToTargetString = MutableLiveData<String?>()
    val distanceToTargetString: LiveData<String?> get() = _distanceToTargetString

    private val _elevationString = MutableLiveData<String?>()
    val elevationString: LiveData<String?> get() = _elevationString

    private val _trekLocations = MutableLiveData<LiveData<TrekLocations>?>()
    val trekLocations: LiveData<LiveData<TrekLocations>?> get() = _trekLocations

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
            marker.icon.setTint(ContextCompat.getColor(app.applicationContext, R.color.theme_blue))
            if (marker.subDescription.isNullOrEmpty()) {
                viewModelScope.launch {
                    marker.subDescription = getLocationInformation(marker.position)

                }
            }

            val elevation = marker.position.altitude
            _elevationString.value = String.format("%.2f m", elevation)
            marker.id?.let {
                _trekLocations.value = repository.getTrek(it.toLong()).asLiveData()
            }
            marker.showInfoWindow()
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
                it.icon.setTint(ContextCompat.getColor(app.applicationContext, R.color.red))
                updatePathPointsToTarget()
            } else {
                it.icon.setTint(ContextCompat.getColor(app.applicationContext, R.color.theme_blue))
            }
        }
    }

    fun clearSelectedMarker() {
        if (_selectedMarker.value != null) {
            _selectedMarker.value?.let {
                it.icon.setTint(ContextCompat.getColor(app.applicationContext, R.color.theme_orange))
                it.infoWindow.close()
            }
            _trekLocations.value = null
            _selectedMarker.value = null
            _distanceToTargetString.value = null
            _elevationString.value = null
            _pathPointsToTarget.value = mutableListOf()
        }
    }

    private fun setDistanceToTargetString(current: GeoPoint) {
        _selectedMarker.value?.let {
            val target: GeoPoint = it.position
            val distance = current.distanceToAsDouble(target)
            _distanceToTargetString.value = String.format("%.2f m", distance)
        }
    }

    private val _addingCustomMarker = MutableLiveData<Boolean>()
    val addingCustomMarker : LiveData<Boolean> get() = _addingCustomMarker
    fun setAddingCustomMarker(isChecked: Boolean) {
        _addingCustomMarker.value = isChecked
    }


    private val _currentPosition = MutableLiveData<GeoPoint>()
    val currentPosition: LiveData<GeoPoint> get() = _currentPosition

    private val _startingPoint = MutableLiveData<GeoPoint>()
    val startingPoint: LiveData<GeoPoint> get() = _startingPoint

    fun refreshList(limit:Int) {
        viewModelScope.launch {
            repository.limitPoints(limit).collect {
                _myPointList.value = it
            }

//            repository.getAllMyPoints().collect {
//                _myPointList.value = it
//            }


        }
    }

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage : LiveData<String?>  get() = _errorMessage

    fun startLocationClient() {
        // This is only for the map, and not for the tracking service
        locationClient.getLocationUpdates(500L)
            .catch { e ->
                e.printStackTrace()
                _errorMessage.value = e.message
            }
            .onEach { location ->
                getLocation.postValue(location)
                //TODO DELETE THIS SIMPLE TEST
                _errorMessage.value = location.altitude.toString()

                Log.d("TrackingViewModel", "Updated getLocation")
                val geo = GeoPoint(location)
                _currentPosition.value = geo

                if (_selectedMarker.value != null) {
                    setDistanceToTargetString(geo)
                    if (_selectedMarkerIsTarget.value == true)
                        updatePathPointsToTarget()
                }

                if (_startingPoint.value == null)
                    _startingPoint.value = geo
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
    }

    // This allows us to get location from other fragments
    companion object {
        val getLocation = MutableLiveData<Location>()
    }

    private fun getLocationInformation(p: GeoPoint): String? {
        val gc = Geocoder(app.applicationContext, Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(p.latitude, p.longitude, 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
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