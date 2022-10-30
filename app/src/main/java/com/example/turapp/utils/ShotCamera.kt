package com.example.turapp.utils


import android.content.Context
import android.hardware.camera2.*
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.turapp.fragments.CameraFragment

class ShotCamera(
    private val mContext: Context,
    private val cameraView: PreviewView
) {

    public fun takePhoto() {}

    public fun captureVideo() {}

    public fun startCamera() {}
}