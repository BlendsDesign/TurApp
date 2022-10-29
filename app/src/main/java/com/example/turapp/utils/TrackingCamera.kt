package com.example.turapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import android.view.TextureView.SurfaceTextureListener
import androidx.core.app.ActivityCompat

class TrackingCamera(private val mContext: Context, private val cameraView: TextureView) {

    private var cameraId: String = ""
    private var imageDimension: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequestBuilder //to get the feed
            : CaptureRequest.Builder? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private val mBackgroundHandler: Handler? = null

    var cameraListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            //eg. transform image capture size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun openCamera() {
        val manager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try { //in case the phone does not have a cam
            cameraId = manager.cameraIdList[0] //0 is the camera on the back of the phone
            val chts = manager.getCameraCharacteristics(cameraId)
            val map = chts.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {}
        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = cameraView.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        cameraCaptureSession = session
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(
                            mContext, "Configuration changed", Toast.LENGTH_LONG
                        ).show()
                    }
                },
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePreview() { //update the modes for the capture request builder
        if (cameraDevice == null) return
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSession!!.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

}