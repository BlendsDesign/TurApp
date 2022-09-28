package com.example.turapp.startPage

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.mapView.roomDb.PoiDatabase
import com.example.turapp.mapView.roomDb.entities.PoiDao
import com.example.turapp.mapView.roomDb.entities.PointOfInterest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

class StartViewModel(app: Application) : ViewModel() {

    // TODO: Implement the ViewModel
    private val _test = MutableLiveData<String>()
    val test: LiveData<String> get() = _test

    private val dao : PoiDao = PoiDatabase.getInstance(app).poiDao
    private val _points = MutableLiveData<List<PointOfInterest>>()
    val points : LiveData<List<PointOfInterest>> get() = _points


    init {
        _points.value = listOf()
        _test.value = "REMOVE THIS LATER"

        viewModelScope.launch {
            _points.value = dao.getAllPointOfInterest()
        }
    }


    fun getMockData(): MutableList<Location> {
        return  mutableListOf(
            Location("Canary River", 111 ),
            Location("Sweet Canyon", 222 ),
            Location("Country Road", 333 ),
            Location("Cotton Fields", 444 ),
            Location("Death Valley", 555 ),
            Location("Scary Forest", 666 ),
            Location("Twin Peaks", 777 ),
            Location("Fishing Spot", 888 ),
            Location("Hunting ground", 999 ),
            Location("Steep Hill", 132 ),
        )
    }




    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StartViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

}