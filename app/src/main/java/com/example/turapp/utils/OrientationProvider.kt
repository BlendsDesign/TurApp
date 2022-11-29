package com.example.turapp.utils

import android.app.Application
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.turapp.utils.Sensors.AccelerometerSensor
import com.example.turapp.utils.Sensors.MagnetoMeterSensor
import com.example.turapp.utils.locationClient.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class OrientationProvider(private val context: Context) {

    private val magnetoMeterSensor = MagnetoMeterSensor(requireNotNull(context))
    private val accelerometerSensor = AccelerometerSensor(context)
    private var _magnetoSensorData = mutableListOf<Float>()
    private var _accSensorData = mutableListOf<Float>()
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun startAccAndMag() {

        magnetoMeterSensor?.let {
            it.setOnSensorValuesChangedListener { reading ->
                _magnetoSensorData = reading as MutableList<Float>
            }
            it.startListening()
        }

        accelerometerSensor?.let { it ->
            it.setOnSensorValuesChangedListener { reading ->
                _accSensorData = reading as MutableList<Float>
            }
            it.startListening()
        }

    }

    fun stopAccAndMag(){
        magnetoMeterSensor.stopListening()
        accelerometerSensor.stopListening()
    }

    //https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
    fun getAzimuth(): Float {
        // Update rotation matrix, which is needed to update orientation angles.
        if (_magnetoSensorData.isEmpty() || _accSensorData.isEmpty())
            return 0f
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            _accSensorData.toFloatArray(),
            _magnetoSensorData.toFloatArray()
        )
        // "rotationMatrix" now has up-to-date information.

        val orient = SensorManager.getOrientation(rotationMatrix, orientationAngles).toMutableList()
        if (orient.isEmpty())
            return 0f

        //convert to degrees from radians
        return Math.toDegrees(orient[0].toDouble()).toFloat()

        //getTrueNorth(azimuth)
    }
}