package com.example.turapp.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING

class SelfieViewModel(private val app: Application, val typeArgument: String): ViewModel() {

    private val _pictureUri = MutableLiveData<Uri?>()
    val pictureUri: LiveData<Uri?> get() = _pictureUri
    private val _keepPicture = MutableLiveData<Boolean>()
    val keepPicture: LiveData<Boolean> get() = _keepPicture
    fun savePicture() {
        _keepPicture.value = true
    }
    fun resetKeepPicture() {
        _keepPicture.value = false
    }
    private val _selectedCamera = MutableLiveData<CameraSelector>()
    val selectedCamera: LiveData<CameraSelector> get() = _selectedCamera
    fun setSelectedCamera() {
        if (_selectedCamera.value != CameraSelector.DEFAULT_FRONT_CAMERA) {
            _selectedCamera.value = CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            _selectedCamera.value = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    init {
        if (typeArgument == TYPE_TRACKING)
            _selectedCamera.value = CameraSelector.DEFAULT_FRONT_CAMERA
        else
            _selectedCamera.value = CameraSelector.DEFAULT_BACK_CAMERA
    }

    fun setPictureUri(uri: Uri?) {
        _pictureUri.value = uri
    }

    fun deleteTakenPicture() {
        try {
            _pictureUri.value?.let {
                app.applicationContext.contentResolver.delete(
                    it, null, null
                )
            }
        } catch (e: Exception) {
            Log.e("SelfieViewModel","Error deleting image", e)
        }
        _pictureUri.value = null
    }

    class Factory(private val app: Application, private val typeArgument: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SelfieViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SelfieViewModel(app, typeArgument) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}