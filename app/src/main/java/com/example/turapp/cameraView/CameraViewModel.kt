package com.example.turapp.cameraView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CameraViewModel: ViewModel() {


    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CameraViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}