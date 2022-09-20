package com.example.turapp.seeLiveSensorData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class LiveSensorDataViewModel: ViewModel()  {

    val text = "Hello this is it"

    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LiveSensorDataViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LiveSensorDataViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}