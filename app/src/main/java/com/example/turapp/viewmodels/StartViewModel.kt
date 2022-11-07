package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.MyRepository
import com.example.turapp.repository.trackingDb.MyPointDAO
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.SimplePoiAndActivities
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.utils.MyPointRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class StartViewModel(app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)
    private val _points = MutableLiveData<List<MyPointWithGeo>>()
    val points : LiveData<List<MyPointWithGeo>> get() = _points
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading


    fun refreshList() {
        _isLoading.value = true

        viewModelScope.launch {
            val temp = repository.getAllMyPointsWithGeo()
            _points.value = temp
            _isLoading.value = false
        }
    }


    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StartViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

}