package com.example.turapp.seeLiveSensorData

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.Sensors.MeasurableSensor
import java.lang.IllegalArgumentException

class LiveSensorDataViewModel(private val testSensor: MeasurableSensor): ViewModel()  {

    private val _sensorData = MutableLiveData<List<Float>>()
    val sensorData: LiveData<List<Float>> get() = _sensorData

    init {
        testSensor.startListening()
        testSensor.setOnSensorValuesChangedListener {
            _sensorData.value = it
        }
    }


    class Factory(private val sensor: MeasurableSensor) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LiveSensorDataViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LiveSensorDataViewModel(sensor) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}