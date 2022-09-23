package com.example.turapp.mapView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModel: ViewModel() {


    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}