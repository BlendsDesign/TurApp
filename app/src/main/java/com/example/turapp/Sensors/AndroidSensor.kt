package com.example.turapp.Sensors

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager

// BASED OFF THIS VIDEO https://www.youtube.com/watch?v=IU-EAtITRRM


abstract class AndroidSensor(
    private val context: Context,
    private val sensorFeature: String,
    sensorType: Int
    ) : MeasurableSensor(sensorType), SensorEventListener {
    override val doesSensorExist: Boolean
        get() = context.packageManager.hasSystemFeature(sensorFeature)

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    override fun startListening() {
        if (!doesSensorExist) {
            if(::sensorManager.isInitialized && sensor == null) {
                sensorManager = context.getSystemService(SensorManager::class.java) as SensorManager
                sensor = sensorManager.getDefaultSensor(sensorType)
            }
            sensor.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun stopListening() {
        if(!doesSensorExist || !::sensorManager.isInitialized) {
            sensor.let {
                sensorManager.unregisterListener(this)
            }
        }
    }
}