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
    private val _accSensorData = MutableLiveData<MutableList<Float>>()
    val accSensorData: LiveData<MutableList<Float>> get() = _accSensorData
    private val _tempAccSensorRec = MutableLiveData<MutableList<MutableList<Float>>>()
    val tempAccSensorRec: LiveData<MutableList<MutableList<Float>>> get() = _tempAccSensorRec

    private val _prevAccData = MutableLiveData<MutableList<Float>>()

    private val _accSensorDataFiltered = MutableLiveData<MutableList<Float>>()
    val accSensorDataFiltered: LiveData<MutableList<Float>> get() = _accSensorDataFiltered

    private val gyroSensor = GyroscopeSensor(app)
    private val _gyroSensorData = MutableLiveData<MutableList<Float>>()
    val gyroSensorData: LiveData<MutableList<Float>> get() = _gyroSensorData
    private val _tempGyroSensorRec = MutableLiveData<MutableList<MutableList<Float>>>()
    val tempGyroSensorRec: LiveData<MutableList<MutableList<Float>>> get() = _tempGyroSensorRec

    private val _prevGyroData = MutableLiveData<MutableList<Float>>()

    private val _gyroSensorDataFiltered = MutableLiveData<MutableList<Float>>()
    val gyroSensorDataFiltered: LiveData<MutableList<Float>> get() = _gyroSensorDataFiltered

    // DB DAO
    private val dao: PoiDao = PoiDatabase.getInstance(app).poiDao

    // Is the data being recorded (to tempSensorRec)
    private val _recording = MutableLiveData<Boolean>()
    val recording: LiveData<Boolean> get() = _recording

    // Used to time the recording
    private var startTime: Long? = null

    private val magnetoSensor = MagnetoMeterSensor(app)
    private val _magnetoSensorData = MutableLiveData<MutableList<Float>>()
    val magnetoSensorData: LiveData<MutableList<Float>> get() = _magnetoSensorData

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val _orientationData = MutableLiveData<List<Float>>()
    val orientationData: LiveData<List<Float>> get() = _orientationData
    private val _orientationRec = MutableLiveData<List<List<Float>>>()
    val orientationRec : LiveData<List<List<Float>>> get() = _orientationRec

    private var gravity = listOf<Float>()

    init {
        magnetoSensor.startListening()
        magnetoSensor.setOnSensorValuesChangedListener {
            _magnetoSensorData.value = it as MutableList<Float>
            updateOrientationAngles()
        }

        accSensor.startListening()
        accSensor.setOnSensorValuesChangedListener {
            // STORE OLD VALUE FOR FILTER
            _prevAccData.value = _accSensorData.value  ?: it as MutableList<Float>
            // Record new value
            _accSensorData.value = it as MutableList<Float>

            _accSensorDataFiltered.value = filterOutGravity(it as MutableList<Float>, _prevAccData.value ?: mutableListOf())
            updateOrientationAngles()
        }

        gravity = _accSensorData.value ?: mutableListOf<Float>()

        gyroSensor.startListening()
        gyroSensor.setOnSensorValuesChangedListener {
            _prevGyroData.value = _gyroSensorData.value  ?: it as MutableList<Float>
            _gyroSensorData.value = it as MutableList<Float>

            _gyroSensorDataFiltered.value = gyroFilter(it, _prevGyroData.value ?: mutableListOf())
        }

    }

    fun startRec() {
        startTime = System.currentTimeMillis()
        _recording.value = true
        // This nullifies list
        _tempAccSensorRec.value = mutableListOf()
        _tempGyroSensorRec.value = mutableListOf()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it as MutableList<Float>

            val temp: MutableList<MutableList<Float>> = _tempAccSensorRec.value ?: mutableListOf()
            temp.add(it as MutableList<Float>)

            _tempAccSensorRec.value = temp
            updateOrientationAngles()
        }
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it as MutableList<Float>
            val temp: MutableList<MutableList<Float>> = _tempGyroSensorRec.value ?: mutableListOf()
            temp.add(it as MutableList<Float>)
            _tempGyroSensorRec.value = temp
        }
    }

    fun stopRec() {
        _recording.value = false
        //accSensor.stopListening()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it as MutableList<Float>
            updateOrientationAngles()
        }
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it as MutableList<Float>
        }
        val endTime = System.currentTimeMillis()
        val timeTaken = endTime - (startTime ?: endTime)
        startTime = null
        storeRecording(timeTaken)

    }

    private fun storeRecording(timeTaken: Long) {
        viewModelScope.launch {
            val poi = PointOfInterest(
                poiTime = LocalDateTime.now().toString(), poiLengt = timeTaken.toFloat(),
                poiName = "TEST REC", poiLong = 0F, poiLat = 0F
            )
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
        if(_magnetoSensorData.value != null && _accSensorData.value != null) {

            // Update rotation matrix, which is needed to update orientation angles.
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                _accSensorData.value?.toFloatArray(),
                _magnetoSensorData.value?.toFloatArray()
            )

            // "rotationMatrix" now has up-to-date information.

            //convert to degrees from radians
            val orientationArr = _orientationData.value?.toFloatArray()

            var azimuth = (orientationArr!![0]*180/ Math.PI)+180 //hvorfor !! her?
            var pitch = (orientationArr[1] * 180/Math.PI)+90 // men ikke her?
            var roll = orientationArr[2]*180/Math.PI

            _orientationData.value = SensorManager.getOrientation(rotationMatrix, orientationAngles).asList()

            // "orientationAngles" now has up-to-date information.
        }
    }
    private fun gyroFilter(event: MutableList<Float>, previousEvent: MutableList<Float>): MutableList<Float> {
        // high-pass filter. From lecture on filtering: Y = a*y1+ a*(x2 - x1)
        //(Gravity new) = alpha * (gravity old) + alpha * (event.values old - event.values new)
        val alpha = 0.8F

        previousEvent[0] = alpha * previousEvent[0] + alpha * (previousEvent[0] - event[0])
        previousEvent[1] = alpha * previousEvent[1] + alpha * (previousEvent[1] - event[0])
        previousEvent[2] = alpha * previousEvent[2] + alpha * (previousEvent[2] - event[0])

        return previousEvent
    }

    //https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-raw-data
    // getting rid of gravity from raw acc data
    private fun filterOutGravity(event: MutableList<Float>, previousEvent: MutableList<Float>) : MutableList<Float> {

        val alpha: Float = 0.8f
        val linearAcceleration = mutableListOf<Float>()

        // Isolate the force of previousEvent with the low-pass filter.
        previousEvent[0] = alpha * previousEvent[0] + (1 - alpha) * event[0]
        previousEvent[1] = alpha * previousEvent[1] + (1 - alpha) * event[1]
        previousEvent[2] = alpha * previousEvent[2] + (1 - alpha) * event[2]

         //Remove the gravity contribution with the high-pass filter.
        linearAcceleration.add(event[0] - previousEvent[0])
        linearAcceleration.add(event[1] - previousEvent[1])
        linearAcceleration.add(event[2] - previousEvent[2])

        return linearAcceleration
    }

    fun generalFilter(listOfRecording: MutableList<MutableList<Float>>):
            MutableList<MutableList<Float>> {
        var lastX: Float = 0F
        var lastY: Float = 0F
        var lastZ: Float = 0F

        val filterWeight: Float = 0.1F

        listOfRecording.forEach {
            lastX = it[0]
            lastY = it[1]
            lastZ = it[2]

            if (it[0] - lastX > 1) {
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