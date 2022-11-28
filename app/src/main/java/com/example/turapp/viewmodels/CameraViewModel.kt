package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.MyPointRepository

class CameraViewModel(app: Application): ViewModel() {

    //send app
    private val repository = MyPointRepository(app)

    private val _takingPicture = MutableLiveData<Boolean>()
    val takingPicture : LiveData<Boolean> get() = _takingPicture

    fun setTakingPicture() { //switch
        _takingPicture.value = _takingPicture.value != true
    }

    class Factory(val app:Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CameraViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}