package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.repository.MyRepository
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.utils.MyPointRepository

class SaveMyPointViewModel(private val app: Application): ViewModel() {

    private val repository: MyPointRepository = MyPointRepository(app)



    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaveMyPointViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SaveMyPointViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}