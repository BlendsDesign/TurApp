package com.example.turapp.viewmodels

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.*
import android.hardware.SensorManager
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.utils.Sensors.AccelerometerSensor
import com.example.turapp.utils.Sensors.GyroscopeSensor
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.PointOfInterest
import com.example.turapp.roomDb.entities.Recording
import kotlinx.coroutines.launch
import com.example.turapp.utils.Sensors.MagnetoMeterSensor
import com.example.turapp.repository.MyRepository
import java.lang.IllegalArgumentException
import kotlin.math.sqrt
import android.content.Context
import com.example.turapp.MainActivity


@Suppress("UNCHECKED_CAST")
class LiveSensorDataViewModel(app: Application) : ViewModel() {

    // DB DAO
    private val dao: PoiDao = PoiDatabase.getInstance(app).poiDao
    private val repository = MyRepository(dao)

    // Are we currently recording
    private val _recording = MutableLiveData<Boolean>()
    val recording: LiveData<Boolean> get() = _recording

    // Used to time the recording
    private var startTime: Long? = null

    // ACC SENSOR
    private val accSensor = AccelerometerSensor(app)
    private val _accSensorData = MutableLiveData<MutableList<Float>>()
    val accSensorData: LiveData<MutableList<Float>> get() = _accSensorData
    private val _accSensorDataFiltered = MutableLiveData<MutableList<Float>>()
    val accSensorDataFiltered: LiveData<MutableList<Float>> get() = _accSensorDataFiltered
    private var _prevAccData = mutableListOf<Float>()
    private val _tempAccSensorRec = mutableListOf<MutableList<Float>>()
    private val _recAccSensorData = MutableLiveData<Boolean>()
    val recAccSensorData: LiveData<Boolean> get() = _recAccSensorData
    fun setRecAccSensorData() {
        _recAccSensorData.value = _recAccSensorData.value != true
    }

    // GYROSENSOR
    private val gyroSensor = GyroscopeSensor(app)
    private val _gyroSensorData = MutableLiveData<MutableList<Float>>()
    val gyroSensorData: LiveData<MutableList<Float>> get() = _gyroSensorData
    private val _gyroSensorDataFiltered = MutableLiveData<MutableList<Float>>()
    private var _prevGyroData = mutableListOf<Float>()
    val gyroSensorDataFiltered: LiveData<MutableList<Float>> get() = _gyroSensorDataFiltered
    private val _tempGyroSensorRec = mutableListOf<MutableList<Float>>()
    private val _recGyroSensorData = MutableLiveData<Boolean>()
    val recGyroSensorData: LiveData<Boolean> get() = _recGyroSensorData
    fun setRecGyroSensorData() {
        _recGyroSensorData.value = _recGyroSensorData.value != true
    }

    // MAGNETOSENSOR
    private val magnetoSensor = MagnetoMeterSensor(app)
    private val _magnetoSensorData = MutableLiveData<List<Float>>()
    val magnetoSensorData: LiveData<List<Float>> get() = _magnetoSensorData
    private val _tempMagnetoSensorRec = mutableListOf<MutableList<Float>>()
    private val _recMagnetoSensorData = MutableLiveData<Boolean>()
    val recMagnetoSensorData: LiveData<Boolean> get() = _recMagnetoSensorData
    fun setRecMagnetoSensorData() {
        _recMagnetoSensorData.value = _recMagnetoSensorData.value != true
    }

    // ORIENTATION
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val _orientationData = MutableLiveData<List<Float>>()
    val orientationData: LiveData<List<Float>> get() = _orientationData
    private val _orientationRec = mutableListOf<List<Float>>()
    private val _recOrientationSensorData = MutableLiveData<Boolean>()
    val recOrientationSensorData: LiveData<Boolean> get() = _recOrientationSensorData
    private var _recOrientation: Boolean = false
    fun setRecOrientationSensorData() {
        _recOrientationSensorData.value = _recOrientationSensorData.value != true
    }
    private var _distData = MutableLiveData<Float>(0.0f)
    val distData: LiveData<Float> get() = _distData

    init {
        magnetoSensor.startListening()
        magnetoSensor.setOnSensorValuesChangedListener {
            _magnetoSensorData.value = it
            updateOrientationAngles()
        }

        accSensor.startListening()
        accSensor.setOnSensorValuesChangedListener {
            // STORE OLD VALUE FOR FILTER
            _prevAccData = _accSensorData.value  ?: it as MutableList<Float>
            // Record new value
            _accSensorData.value = it as MutableList<Float>
            _accSensorDataFiltered.value = filterOutGravity(it, _prevAccData)
            updateOrientationAngles()
            calcAcc(it)
        }
        gyroSensor.startListening()
        gyroSensor.setOnSensorValuesChangedListener {
            _prevGyroData = _gyroSensorData.value ?: it as MutableList<Float>
            _gyroSensorData.value = it as MutableList<Float>
            _gyroSensorDataFiltered.value = gyroFilter(it, _prevGyroData)
        }

    }

    private fun calcAcc(input: List<Float>) {
        _distData.value = sqrt(input[0]*input[0]+input[1]*input[1]+input[2]*input[2]) - 9.81f
    }

    fun startRec() {
        // Empty all existing temp
        _tempAccSensorRec.clear()
        _tempGyroSensorRec.clear()
        _tempMagnetoSensorRec.clear()
        _orientationRec.clear()

        // Checking if switch is set to true
        if (_recAccSensorData.value == true) {
            accSensor.setOnSensorValuesChangedListener { data ->
                _accSensorData.value = data as MutableList<Float>
                _tempAccSensorRec.add(data)
                updateOrientationAngles()
            }
        }
        if (_recGyroSensorData.value == true) {
            gyroSensor.setOnSensorValuesChangedListener { data ->
                _gyroSensorData.value = data as MutableList<Float>
                _tempGyroSensorRec.add(data)
            }
        }
        if (_recMagnetoSensorData.value == true) {
            magnetoSensor.setOnSensorValuesChangedListener { data ->
                _magnetoSensorData.value = data as MutableList<Float>
                _tempMagnetoSensorRec.add(data)
                updateOrientationAngles()
            }
        }
        if (_recOrientationSensorData.value == true) {
            _recOrientation = true
        }
        startTime = System.currentTimeMillis()
        _recording.value = true
    }

    fun stopRec() {
        _recording.value = false
        _recOrientation = false
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it as MutableList<Float>
            updateOrientationAngles()
        }
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it as MutableList<Float>
        }
        magnetoSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it as MutableList<Float>
        }
        val endTime = System.currentTimeMillis()
        val timeTaken = endTime - (startTime ?: endTime)
        storeRecording(startTime?: endTime, timeTaken)
        startTime = null
    }

    private fun storeRecording(startT: Long, timeTaken: Long) {
        viewModelScope.launch {
            val poi = PointOfInterest( poiLengt = timeTaken, createdAt = startT)
            val recs = mutableListOf<Recording>()
            if (_tempAccSensorRec.size > 0) {
                recs.add(
                    Recording( sensorType = Sensor.TYPE_ACCELEROMETER,
                        recording = _tempAccSensorRec
                    )
                )
            }
            if (_tempGyroSensorRec.size > 0) {
                recs.add(
                    Recording( sensorType = Sensor.TYPE_GYROSCOPE,
                        recording = _tempGyroSensorRec
                    )
                )
            }
            if (_tempMagnetoSensorRec.size > 0) {
                recs.add(
                    Recording( sensorType = Sensor.TYPE_MAGNETIC_FIELD,
                        recording = _tempMagnetoSensorRec
                    )
                )
            }
            if (_orientationRec.size > 0) {
                recs.add(
                    Recording( sensorType = Sensor.TYPE_ORIENTATION,
                        recording = _orientationRec as MutableList<MutableList<Float>>
                    )
                )
            }
            repository.addPoiAndRecordings(poi, recs)
        }
    }

    //https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
    private fun updateOrientationAngles() {
        if (_magnetoSensorData.value != null && _accSensorData.value != null) {

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
//            var azimuth = (orientationArr!![0]*180/ Math.PI)+180 //hvorfor !! her?
//            var pitch = (orientationArr[1] * 180/Math.PI)+90 // men ikke her?
//            var roll = orientationArr[2]*180/Math.PI
            val o = SensorManager.getOrientation(rotationMatrix, orientationAngles).asList()
            _orientationData.value = o
            if (_recOrientation) {
                _orientationRec.add(o)
            }
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

        val alpha = 0.8f
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