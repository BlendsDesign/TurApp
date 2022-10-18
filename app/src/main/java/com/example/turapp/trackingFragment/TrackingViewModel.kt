package com.example.turapp.trackingFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class TrackingViewModel(private val app: Application): ViewModel() {


    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TrackingViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}