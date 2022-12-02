package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.turapp.repository.MyPointRepository

class DisplayTrekViewModel(app: Application, id: Long): ViewModel() {

    private val repository = MyPointRepository(app)
    val myPoint = repository.getMyPoint(id).asLiveData()
    val trek = repository.getTrek(id).asLiveData()






    class Factory(private val app: Application, private val id: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DisplayTrekViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DisplayTrekViewModel(app, id) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}