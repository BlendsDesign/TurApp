package com.example.turapp.viewmodels

import android.app.Application
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.utils.locationClient.LocationService
import org.osmdroid.util.GeoPoint

class NowTrackingViewModel(private val app: Application): ViewModel() {

    private val _currentLocation = LocationService.currentLocation
    val currentLocation : LiveData<Location> get() = _currentLocation

    private val _tracked = LocationService.trackedPoints
    val tracked : LiveData<MutableList<MutableList<GeoPoint>>> get() = _tracked

    private val _timer = LocationService.timerHundreds
    val timer: LiveData<Long> get() = _timer

    init {
        Intent(app.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START_OR_RESUME_SERVICE
            app.applicationContext.startService(this)
        }
    }


    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NowTrackingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NowTrackingViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}