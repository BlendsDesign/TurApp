package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.MyPointRepository

class ArViewModel (app: Application): ViewModel(){

    private val repository = MyPointRepository(app)


    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ArViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}