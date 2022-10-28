package com.example.turapp.utils.locationClient

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.example.turapp.R
import com.example.turapp.utils.helperFiles.CHANNEL_ID
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var locationClient: LocationClient

    private var tracking: Boolean = false

    private var pausedTime: Long? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
            ACTION_SWITCH_TRACKING -> switchTracking()
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun start() {
        Log.d("LocationService", "Started service")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setSilent(true)
            .setVisibility(VISIBILITY_PUBLIC)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Below here is where we want to do the magic and add points to the database
        // or viewmodel list (I dont't think viewModel will work but hey, can try)
        // interval sets how often it is called SET APPROPRIATELY
        locationClient.getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val test = "Location: ($lat, $long)"

                if (tracking) {
                    if (pausedTime != null) {
                        pausedTime = null
                        //TODO Save location in database with Restart Info
                    } else {
                        //TODO Save location in database
                    }
                }

                Log.d("newLocation", test)
                val updatedNotification = notification.setContentText(
                    "Location: ($lat, $long)"
                )
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
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
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SWITCH_TRACKING = "ACTION_SWITCH_TRACKING"
    }
}