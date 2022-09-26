package com.example.turapp.seeLiveSensorData

import android.app.Application
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.Sensors.AccelerometerSensor
import com.example.turapp.Sensors.GyroscopeSensor
import com.example.turapp.Sensors.MagnetoMeterSensor


import java.lang.IllegalArgumentException

class LiveSensorDataViewModel(private val app: Application): ViewModel()  {

    private val accSensor = AccelerometerSensor(app)
    private val _accSensorData = MutableLiveData<List<Float>>()
    val accSensorData: LiveData<List<Float>> get() = _accSensorData

    private val gyroSensor = GyroscopeSensor(app)
    private val _gyroSensorData = MutableLiveData<List<Float>>()
    val gyroSensorData: LiveData<List<Float>> get() = _gyroSensorData

    private val magnetoSensor = MagnetoMeterSensor(app)
    private val _magnetoSensorData = MutableLiveData<List<Float>>()
    val magnetoSensorData: LiveData<List<Float>> get() = _magnetoSensorData

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)


    // TEMP FAKE database
    val dbFake = MutableLiveData<String>()

    init {
        accSensor.startListening()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it
        }
        gyroSensor.startListening()
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it
            var temp: String = dbFake.value + it.toString()
            dbFake.value = temp
        }
        magnetoSensor.startListening()
        magnetoSensor.setOnSensorValuesChangedListener {
            _magnetoSensorData.value = it
            updateOrientationAngles()
        }
    }

    //https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            _accSensorData.value?.toFloatArray(),
            _magnetoSensorData.value?.toFloatArray()
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.
    }




    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LiveSensorDataViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LiveSensorDataViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}