package com.example.turapp.viewmodels

import android.app.Application
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.MyPointRepository
import com.example.turapp.utils.locationClient.LocationService
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class NowTrackingViewModel(private val app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)

    private val _currentLocation = LocationService.currentLocation
    val currentLocation: LiveData<Location> get() = _currentLocation

    private val _tracked = LocationService.trackedPoints
    val tracked: LiveData<MutableList<MutableList<GeoPoint>>> get() = _tracked

    private val _timer = LocationService.timerHundreds
    val timer: LiveData<Long> get() = _timer

    private val _steps = LocationService.steps
    val steps: LiveData<Int> get() = _steps

    private val _finishedSaving = MutableLiveData<Boolean>()
    val finishedSaving: LiveData<Boolean> get() = _finishedSaving
    fun resetFinishedSaving() {
        _finishedSaving.value = false
    }

    init {
        Intent(app.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START_OR_RESUME_SERVICE
            app.applicationContext.startService(this)
        }
    }

    fun stopService() {
        viewModelScope.launch {
            repository.insertMyPoint(MyPoint(title = "My Run",
                type = TYPE_TRACKING))
            Intent(app.applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                app.applicationContext.startService(this)
            }
            _finishedSaving.value = true
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

    override fun onCleared() {
        super.onCleared()
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