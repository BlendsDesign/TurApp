package com.example.turapp.viewmodels

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.*
import com.example.turapp.repository.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.PointOfInterest
import com.example.turapp.roomDb.entities.Recording
import com.example.turapp.utils.Sensors.AccelerometerSensor
import com.example.turapp.utils.Sensors.GyroscopeSensor
import com.example.turapp.utils.Sensors.MagnetoMeterSensor
import com.example.turapp.utils.Sensors.SensorFilterFunctions
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class ArViewModel (app: Application): ViewModel(){

    // DB DAO
    private val dao: PoiDao = PoiDatabase.getInstance(app).poiDao
    private val repository = MyRepository(dao)

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

    // MAGNETOSENSOR
    private val magnetoSensor = MagnetoMeterSensor(app)
    private val _magnetoSensorData = MutableLiveData<MutableList<Float>>()
    val magnetoSensorData: LiveData<MutableList<Float>> get() = _magnetoSensorData
    private val _magSensorDataFiltered = MutableLiveData<MutableList<Float>>()
    val magSensorDataFiltered: LiveData<MutableList<Float>> get() = _magSensorDataFiltered
    private var _prevMagData = mutableListOf<Float>()
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

    private var accelVec = 0.0f
    private var prevAccelVec = 0.0f

    init {
        val filter = SensorFilterFunctions()

        magnetoSensor.startListening()
        magnetoSensor.setOnSensorValuesChangedListener {

            if(!filter.limitValues(accelVec,prevAccelVec, 0.5f)) {
                _prevMagData = _magnetoSensorData.value ?: it as MutableList<Float>
                _magnetoSensorData.value = it as MutableList<Float>
                _magSensorDataFiltered.value = filter.lowPass(it, 0.2f)
            }
            updateOrientationAngles()
        }

        accSensor.startListening()
        accSensor.setOnSensorValuesChangedListener {
            // STORE OLD VALUE FOR FILTER
            _prevAccData = _accSensorData.value  ?: it as MutableList<Float>
            // Record new value
            _accSensorData.value = it as MutableList<Float>
            _accSensorDataFiltered.value = filter.filterOutGravity(it, _prevAccData)
            updateOrientationAngles()
            prevAccelVec = sqrt(_prevAccData[0]*_prevAccData[0] +
                    _prevAccData[1]*_prevAccData[1] + _prevAccData[2]*_prevAccData[2])
            accelVec = sqrt(it[0]*it[0] + it[1]*it[1] + it[2]*it[2])
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

            val o = SensorManager.getOrientation(rotationMatrix, orientationAngles).toMutableList()

            //convert to degrees from radians
            val yaw = Math.toDegrees(o[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(o[1].toDouble()).toFloat()
            val roll = Math.toDegrees(o[2].toDouble()).toFloat()
            o[0] = yaw
            o[1] = pitch
            o[2] = roll

            _orientationData.value = o
            if (_recOrientation) {
                _orientationRec.add(o)
            }

            // "orientationAngles" now has up-to-date information.
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ArViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}