package com.example.turapp.trackingFragment

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.helperFiles.MyLocationListener
import com.example.turapp.helperFiles.STARTING_POINT
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker



class TrackingViewModel(private val app: Application): ViewModel() {

    private var locationListener = MyLocationListener(app)

    private val _markersList = MutableLiveData<List<Marker>>()
    val markersList : LiveData<List<Marker>> get() = _markersList

    val locGeoPoint: LiveData<GeoPoint> = locationListener.locGeoPoint

    val pas = STARTING_POINT

    private val _isTracking = MutableLiveData<Boolean>()
    val isTracking : LiveData<Boolean> get() = _isTracking
    fun switchIsTracking() {
        _isTracking.value = _isTracking.value != true
    }



    init {
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