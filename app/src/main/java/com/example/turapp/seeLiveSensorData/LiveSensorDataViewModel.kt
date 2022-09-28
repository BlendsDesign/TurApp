package com.example.turapp.seeLiveSensorData

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.*
import com.example.turapp.Sensors.AccelerometerSensor
import com.example.turapp.Sensors.GyroscopeSensor
import com.example.turapp.mapView.roomDb.PoiDatabase
import com.example.turapp.mapView.roomDb.entities.PoiDao
import com.example.turapp.mapView.roomDb.entities.PointOfInterest
import com.example.turapp.mapView.roomDb.entities.Recording
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.time.LocalDateTime


class LiveSensorDataViewModel(app: Application) : ViewModel() {

    private val accSensor = AccelerometerSensor(app)
    private val _accSensorData = MutableLiveData<List<Float>>()
    val accSensorData: LiveData<List<Float>> get() = _accSensorData

    private val gyroSensor = GyroscopeSensor(app)
    private val _gyroSensorData = MutableLiveData<List<Float>>()
    val gyroSensorData: LiveData<List<Float>> get() = _gyroSensorData

    private val _tempSensorData = MutableLiveData<MutableList<MutableList<Float>>>()
    val tempSensorData: LiveData<MutableList<MutableList<Float>>> get() = _tempSensorData

    // TEMP FAKE database
    val dbFake = MutableLiveData<String>()

    // ACTUAL DB
    private val dao: PoiDao = PoiDatabase.getInstance(app).poiDao

    private val _recording = MutableLiveData<Boolean>()
    val recording: LiveData<Boolean> get() = _recording


    init {
        _accSensorData.value = mutableListOf()
        _tempSensorData.value = mutableListOf()
        gyroSensor.startListening()
        gyroSensor.setOnSensorValuesChangedListener {
            _gyroSensorData.value = it
        }
    }


    fun startRec() {
        _recording.value = true
        val emptyList = mutableListOf<MutableList<Float>>()
        _tempSensorData.value = emptyList
        accSensor.startListening()
        accSensor.setOnSensorValuesChangedListener {
            _accSensorData.value = it

            val temp: MutableList<MutableList<Float>> = _tempSensorData.value ?: mutableListOf()
            temp.add(it as MutableList<Float>)

            _tempSensorData.value = temp
        }

    }

    fun stopRec() {
        _recording.value = false
        accSensor.stopListening()
        accSensor.setOnSensorValuesChangedListener { }
        storeRecording()
        // SendDataToDb(rec)
    }

    private fun storeRecording() {
        viewModelScope.launch {
            val poi = PointOfInterest(poiTime = LocalDateTime.now().toString(), poiLengt =  0F,
                poiName =  "TEST REC", poiLong =  0F, poiLat =  0F)
            val id = dao.insertPoi(poi)

            dao.insertRecording(Recording(poiId = id.toInt(), sensorType = Sensor.TYPE_ACCELEROMETER,
                recording = _tempSensorData.value.toString()))
        }
    }

    fun filter(listOfRecording: MutableList<MutableList<Float>>): MutableList<MutableList<Float>> {
        var lastX: Float = listOfRecording[0][0]
        var lastY: Float = listOfRecording[0][0]
        var lastZ: Float = listOfRecording[0][0]
        val filterWeight: Float = 0.1F
        listOfRecording.forEach {
            if (it[0] - lastX > 1) {
                it[0] = lastX + ((it[0] - lastX) * filterWeight)
            }
            lastX = it[0]
            lastY = it[1]
            lastZ = it[2]
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