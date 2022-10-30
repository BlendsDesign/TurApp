package com.example.turapp.utils.helperFiles

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions


object PermissionCheckUtility {

    fun hasLocationPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    fun hasActivityRecognitionPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

    fun hasCameraPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.CAMERA
        )

}