package com.example.turapp.seeLiveSensorData

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.*
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.Sensors.AccelerometerSensor
import com.example.turapp.Sensors.GyroscopeSensor
import com.example.turapp.mapView.roomDb.PoiDatabase
import com.example.turapp.mapView.roomDb.entities.PoiDao
import com.example.turapp.mapView.roomDb.entities.PointOfInterest
import com.example.turapp.mapView.roomDb.entities.Recording
import kotlinx.coroutines.launch
import com.example.turapp.Sensors.MagnetoMeterSensor


import java.lang.IllegalArgumentException
import java.time.LocalDateTime


class LiveSensorDataViewModel(app: Application) : ViewModel() {

    private val accSensor = AccelerometerSensor(app)
    private val _accSensorData = MutableLiveData<List<Float>>()
    val accSensorData: LiveData<List<Float>> get() = _accSensorData
    private val _tempAccSensorRec = MutableLiveData<MutableList<MutableList<Float>>>()
    val tempAccSensorRec: LiveData<MutableList<MutableList<Float>>> get() = _tempAccSensorRec

    private val gyroSensor = GyroscopeSensor(app)
    private val _gyroSensorData = MutableLiveData<List<Float>>()
    val gyroSensorData: LiveData<List<Float>> get() = _gyroSensorData
    private val _tempGyroSensorRec = MutableLiveData<MutableList<MutableList<Float>>>()
    val tempGyroSensorRec: LiveData<MutableList<MutableList<Float>>> get() = _tempGyroSensorRec

    // DB DAO
    private val dao: PoiDao = PoiDatabase.getInstance(app).poiDao

    // Is the data being recorded (to tempSensorRec)
    private val _recording = MutableLiveData<Boolean>()
    val recording: LiveData<Boolean> get() = _recording
    // Used to time the recording
    private var startTime: Long? = null



    private val magnetoSensor = MagnetoMeterSensor(app)
    private val _magnetoSensorData = MutableLiveData<List<Float>>()
    val magnetoSensorData: LiveData<List<Float>> get() = _magnetoSensorData

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val _orientation = MutableLiveData<List<Float>>()
    val orientation: LiveData<List<Float>> get() = _orientation

    private val _tempSensorData = MutableLiveData<MutableList<MutableList<Float>>>()
    val tempSensorData: LiveData<MutableList<MutableList<Float>>> get() = _tempSensorData


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
        }
            magnetoSensor.startListening()
            magnetoSensor.setOnSensorValuesChangedListener {
            _magnetoSensorData.value = it
            updateOrientationAngles() //check: no crash when function is called
        }
    }


    fun startRec() {
        startTime = System.currentTimeMillis()
        _recording.value = true
        // This nullifies list
        _tempAccSensorRec.value = mutableListOf()
        _tempGyroSensorRec.value = mutableListOf()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it

            val temp: MutableList<MutableList<Float>> = _tempAccSensorRec.value ?: mutableListOf()
            temp.add(it as MutableList<Float>)

            _tempAccSensorRec.value = temp
        }
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it
            val temp: MutableList<MutableList<Float>> = _tempGyroSensorRec.value ?: mutableListOf()
            temp.add(it as MutableList<Float>)
            _tempGyroSensorRec.value = temp
        }

    }

    fun stopRec() {
        _recording.value = false
        //accSensor.stopListening()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it
        }
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it
        }
        val endTime = System.currentTimeMillis()
        val timeTaken = endTime - (startTime?: endTime)
        startTime = null
        storeRecording(timeTaken)
        val rec = filter(_tempSensorData)
    }

    private fun storeRecording(timeTaken: Long) {
        viewModelScope.launch {
            val poi = PointOfInterest(poiTime = LocalDateTime.now().toString(), poiLengt = timeTaken.toFloat(),
                poiName =  "TEST REC", poiLong =  0F, poiLat =  0F)
            val id = dao.insertPoi(poi)

            dao.insertRecording(Recording(poiId = id.toInt(), sensorType = Sensor.TYPE_ACCELEROMETER,
                recording = _tempAccSensorRec.value.toString()))
            dao.insertRecording(Recording(poiId = id.toInt(), sensorType = Sensor.TYPE_GYROSCOPE,
                recording = _tempGyroSensorRec.value.toString()))
        }
    }

    //https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
    private fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            _accSensorData.value?.toFloatArray(),
            _magnetoSensorData.value?.toFloatArray()
        )

        // "rotationMatrix" now has up-to-date information.

        _orientation.value = SensorManager.getOrientation(rotationMatrix, orientationAngles).asList()

        // "orientationAngles" now has up-to-date information.
    }

    fun filter(listOfRecording : MutableList<MutableList<Float>>) : MutableList<MutableList<Float>>
    {
        var lastX : Float = 0F
        var lastY : Float = 0F
        var lastZ : Float = 0F

        var filterWeight: Float = 0.1F

        listOfRecording.forEach {
            lastX = it[0]
            lastY = it[1]
            lastZ = it[2]

            if(it[0] - lastX > 1)
            {
                it[0] = lastX + ((it[0] - lastX) * filterWeight)
            }
        }
        return listOfRecording
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