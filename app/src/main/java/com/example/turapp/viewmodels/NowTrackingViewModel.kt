package com.example.turapp.viewmodels

import android.app.Application
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.repository.MyPointRepository
import com.example.turapp.utils.locationClient.LocationService
import org.osmdroid.util.GeoPoint

class NowTrackingViewModel(private val app: Application) : ViewModel() {

    private val _currentLocation = LocationService.currentLocation
    val currentLocation: LiveData<Location> get() = _currentLocation

    private val _tracked = LocationService.trackedPoints
    val tracked: LiveData<MutableList<MutableList<GeoPoint>>> get() = _tracked

    private val _timer = LocationService.timerHundreds
    val timer: LiveData<Long> get() = _timer

    private val _steps = LocationService.steps
    val steps: LiveData<Int> get() = _steps

    private val _distance = LocationService.distance
    val distance: LiveData<Float> get() = _distance

    private val _hasStoppedService = MutableLiveData<Boolean>()
    val hasStoppedService: LiveData<Boolean> get() = _hasStoppedService
    fun resetFinishedSaving() {
        _hasStoppedService.value = false
    }

    init {
        companionTreck = null
        companionTimeInHundreds = null
        companionSteps = null
        companionDistance = null
        Intent(app.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START_OR_RESUME_SERVICE
            app.applicationContext.startService(this)
        }
    }

    fun saveTreck() {
        _tracked.value?.let {
            companionTreck = it
        }
        _steps.value?.let {
            companionSteps = it
        }
        _timer.value?.let {
            companionTimeInHundreds = it
        }
        _distance.value?.let {
            companionDistance = it
        }
        _timer.value?.let {
            companionTimeInHundreds = it
        }
        stopService()
        _hasStoppedService.value = true
    }

    fun cancelTreck() {
        stopService()
        _hasStoppedService.value = true
    }

    private fun stopService() {
        Intent(app.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            app.applicationContext.startService(this)
        }
    }

    fun pauseService() {
        Intent(app.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_PAUSE_TRACKING
            app.applicationContext.startService(this)
        }
    }

    fun resumeService() {
        Intent(app.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START_OR_RESUME_SERVICE
            app.applicationContext.startService(this)
        }
    }

    companion object {
        private var companionTreck: MutableList<MutableList<GeoPoint>>? = null
        private var companionTimeInHundreds: Long? = null
        private var companionSteps: Int? = null
        private var companionDistance: Float? = null
        fun getTreck(): MutableList<MutableList<GeoPoint>>? {
            return companionTreck
        }

        fun getTimeInHundreds(): Long? {
            return companionTimeInHundreds
        }

        fun getSteps(): Int? {
            return companionSteps
        }

        fun getDistance(): Float? = companionDistance
    }

    override fun onCleared() {
        super.onCleared()
        companionTreck = null
        companionTimeInHundreds = null
        companionSteps = null
        companionDistance = null
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