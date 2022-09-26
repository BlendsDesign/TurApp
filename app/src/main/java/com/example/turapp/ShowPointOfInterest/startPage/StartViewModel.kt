package com.example.turapp.ShowPointOfInterest.startPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class StartViewModel(testText: String) : ViewModel() {
    // TODO: Implement the ViewModel
    private val _test = MutableLiveData<String>()
    val test: LiveData<String> get() = _test

    init {
        _test.value = testText
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




    class Factory(val testText: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StartViewModel(testText) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

}