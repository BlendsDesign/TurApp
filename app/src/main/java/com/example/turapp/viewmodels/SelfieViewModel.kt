package com.example.turapp.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SelfieViewModel(private val app: Application): ViewModel() {

    private val _pictureUri = MutableLiveData<Uri?>()
    val pictureUri: LiveData<Uri?> get() = _pictureUri
    private val _keepPicture = MutableLiveData<Boolean>()
    val keepPicture: LiveData<Boolean> get() = _keepPicture
    fun savePicture() {
        _keepPicture.value = true
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

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SelfieViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SelfieViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}