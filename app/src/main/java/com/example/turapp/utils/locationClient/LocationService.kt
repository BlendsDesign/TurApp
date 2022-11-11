package com.example.turapp.utils.locationClient

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.lifecycle.MutableLiveData
import com.example.turapp.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.osmdroid.util.GeoPoint

typealias mPolyline = MutableList<GeoPoint>
typealias mPolylines = MutableList<mPolyline>

class LocationService: Service() {

    private val _trackedPoints: mPolylines = mutableListOf()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var locationClient: LocationClient

    private var tracking: Boolean = false

    private var serviceIsStarting: Boolean = true

    private var timeTracked: Long = 0

    private var pausedTime: Long? = null


    private val notification = NotificationCompat.Builder(this, "location")
        .setContentTitle("Tracking location...")
        .setContentText("Time: null")
        .setSmallIcon(R.drawable.ic_logo)
        .setOngoing(true)
        .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        .setSilent(true)
        .setVisibility(VISIBILITY_PUBLIC)

    private lateinit var notificationManager: NotificationManager
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        trackedPoints.postValue(_trackedPoints)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START_OR_RESUME_SERVICE -> startOrResumeService()
            ACTION_PAUSE_TRACKING -> switchTracking()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startOrResumeService() {
        if(serviceIsStarting) {
            _trackedPoints.add(mutableListOf())
            tracking = true
            serviceIsStarting = false
            startOrResumeTimer()
            val list = mutableListOf<mPolyline>()
            trackedPoints.postValue(list)
            startForeground(1, notification.build())
            start()
        } else if(!tracking) {
            _trackedPoints.add(mutableListOf())
            switchTracking()
            startOrResumeTimer()
            start()
        }
    }


    private fun start() {
        if (tracking)
        Log.d("LocationService", "Started service")

        // Below here is where we want to do the magic and add points to the database
        // or viewmodel list (I dont't think viewModel will work but hey, can try)
        // interval sets how often it is called SET APPROPRIATELY
        locationClient.getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude
                val long = location.longitude
                val test = "Location: ($lat, $long)"
                currentLocation.postValue(location)
                val geo = GeoPoint(location)
                if (tracking) {
                    _trackedPoints.last().add(geo)
                    trackedPoints.postValue(_trackedPoints)
                }
                Log.d("newLocation", test)
            }
            .launchIn(serviceScope)


    }

    private fun startOrResumeTimer() {
        serviceScope.launch {
            while(tracking) {
                timeTracked += 1
                timerHundreds.postValue(timeTracked)
                if (timeTracked % 100 == 0L) {
                    val updatedNotification = notification.setContentText(
                        "Time: ${timeTracked / 100}"
                    )
                    notificationManager?.notify(1, updatedNotification.build())
                }
                delay(10)
            }
        }
    }

    private fun switchTracking() {
        if (tracking) {
            pausedTime = System.currentTimeMillis()
        }
        tracking = tracking != true
    }

    private fun stop() {
        // Remove the notification
        stopForeground(true)
        // Stop the service
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }



    companion object {
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
        const val ACTION_PAUSE_TRACKING = "ACTION_PAUSE_TRACKING"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SWITCH_TRACKING = "ACTION_SWITCH_TRACKING"
        val currentLocation = MutableLiveData<Location>()
        val trackedPoints = MutableLiveData<mPolylines>()
        val timerHundreds  = MutableLiveData<Long>()
    }
}