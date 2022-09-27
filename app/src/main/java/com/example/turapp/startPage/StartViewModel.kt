package com.example.turapp.startPage

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.mapView.roomDb.PoiDatabase
import com.example.turapp.mapView.roomDb.entities.PoiDao
import com.example.turapp.mapView.roomDb.entities.PointOfInterest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class StartViewModel(app: Application) : ViewModel() {

    // TODO: Implement the ViewModel
    private val _test = MutableLiveData<String>()
    val test: LiveData<String> get() = _test


    init {
        _test.value = "REMOVE THIS LATER"

        // Set up application
        val dao: PoiDao = PoiDatabase.getInstance(app.applicationContext).poiDao

        viewModelScope.launch {
            val pointsOfInterest = getMockPoiData()
            pointsOfInterest.forEach {
                dao.insertPoi(it)
            }
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
    fun getMockPoiData(): List<PointOfInterest> {
        return listOf(
            PointOfInterest(poiName = "Test1", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test2", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test3", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test4", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test5", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test6", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test7", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test8", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test9", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test10", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test11", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test12", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test13", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test14", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test15", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
            PointOfInterest(poiName = "Test16", poiLengt = 0F, poiLat = 0.3214F, poiLong = 0F, poiTime = 0F),
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