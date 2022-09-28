package com.example.turapp.seeLiveSensorData

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.lifecycle.*
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.Sensors.AccelerometerSensor
import com.example.turapp.Sensors.GyroscopeSensor
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.PointOfInterest
import com.example.turapp.roomDb.entities.Recording
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

//    private val _tempSensorData = MutableLiveData<MutableList<MutableList<Float>>>()
//    val tempSensorData: LiveData<MutableList<MutableList<Float>>> get() = _tempSensorData

    private val _orientationData = MutableLiveData<MutableList<MutableList<Float>>>()
    val orientationData: LiveData<MutableList<MutableList<Float>>> get() = _orientationData

    private var gravity = listOf<Float>()

    init {
        accSensor.startListening()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it
        }

        gravity = _accSensorData.value?: mutableListOf<Float>()

        gyroSensor.startListening()
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it
        }

        magnetoSensor.startListening()
        magnetoSensor.setOnSensorValuesChangedListener {
        _magnetoSensorData.value = it
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
        updateOrientationAngles()
    }

    private fun storeRecording(timeTaken: Long) {
        viewModelScope.launch {
            val poi = PointOfInterest(poiTime = LocalDateTime.now().toString(), poiLengt = timeTaken.toFloat(),
                poiName =  "TEST REC", poiLong =  0F, poiLat =  0F)
            val id = dao.insertPoi(poi)

            dao.insertRecording(
                Recording(poiId = id.toInt(), sensorType = Sensor.TYPE_ACCELEROMETER,
                recording = _tempAccSensorRec.value.toString())
            )
            dao.insertRecording(
                Recording(poiId = id.toInt(), sensorType = Sensor.TYPE_GYROSCOPE,
                recording = _tempGyroSensorRec.value.toString())
            )
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

    //  Fra leksjon:
    //  Low-pass: Yn=B*Y2+(1-B)y
    //  (Gravity new) = alpha * (gravity old) + (1-alpha)*event.values
    //  gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0] *
    //  gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
    //  gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

    //  High-pass: Y = a*y1+ a*(x2 - x1)
    //  (Gravity new) = alpha * (gravity old) + alpha * (event.values old - event.values new)
    //  gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
    //  gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
    //  gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]


//    fun accelerometerFilterLowPass(accListRecording : MutableList<List<Float>>) {
//        val alpha: Float = 0.8f
//
//        var lastX : Float = 0F
//        var lastY : Float = 0F
//        var lastZ : Float = 0F
//
//        accListRecording.forEach {
//            lastX = alpha * it[0] + (1 - alpha) * event.values[0] //*
//            lastY = it[1]
//            lastZ = it[2]
//
//            if(it[0] - lastX > 1)
//            {
//                it[0] = lastX + ((it[0] - lastX) * filterWeight)
//            }
//        }
//
//    }

    fun gyroFilter() {

    }



    //https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-raw-data
    // getting rid of gravity from raw acc data
        fun sensorFilter(event: SensorEvent) {
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.

            val alpha: Float = 0.8f

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            // Remove the gravity contribution with the high-pass filter.
//            linear_acceleration[0] = event.values[0] - gravity[0]
//            linear_acceleration[1] = event.values[1] - gravity[1]
//            linear_acceleration[2] = event.values[2] - gravity[2]
        }

    fun generalFilter(listOfRecording : MutableList<MutableList<Float>>) :
            MutableList<MutableList<Float>>
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