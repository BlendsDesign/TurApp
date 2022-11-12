package com.example.turapp.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.turapp.fragments.SelfieFragment
import java.text.SimpleDateFormat
import java.util.*

class SelfieCamera(
    private val mContext: Context,
    private val cameraView: PreviewView,
    private val selfieFragment: SelfieFragment
) {
    private var imageCapture: ImageCapture? = null
    private var name : String? = null

    // TODO Add a switch to change to frontfacing camera.
    //  Look at fun startCamera() and val cameraSelector

    fun getPath() : String {
        //complete path, not accessible when using API 30 and up
        return "${Environment.getExternalStorageDirectory()}/$name"
    }

    fun takePhoto(myCallBack : ImageCapture.OnImageSavedCallback) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(mContext.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(mContext), myCallBack

        )

    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Initialize our Preview object, call build on it, get a surface provider
            // from viewfinder, and then set it on the preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    selfieFragment, cameraSelector, preview, imageCapture)
                //There are a few ways this code could fail, like if the app is no longer in focus
            } catch(exc: Exception) {
                Log.e("SelfieCamera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(mContext))
    }
}