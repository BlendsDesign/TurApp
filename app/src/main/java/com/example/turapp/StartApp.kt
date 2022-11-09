package com.example.turapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import com.example.turapp.utils.helperFiles.CHANNEL_ID
import com.example.turapp.utils.helperFiles.Helper
import org.osmdroid.library.BuildConfig

class StartApp: Application() {

    override fun onCreate() {
        super.onCreate()
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Helper.suggestedFix(contextWrapper = ContextWrapper(requireNotNull(applicationContext)))
        val channel = NotificationChannel(
            "location",
            "Location",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}