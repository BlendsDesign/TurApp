package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.SimplePoiAndActivities
import com.example.turapp.roomDb.entities.PoiDao
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class StartViewModel(app: Application) : ViewModel() {

    private val dao : PoiDao = PoiDatabase.getInstance(app).poiDao
    private val repository = MyRepository(dao)
    private val _points = MutableLiveData<List<SimplePoiAndActivities>>()
    val points : LiveData<List<SimplePoiAndActivities>> get() = _points
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading

    init {
        _isLoading.value = true

        viewModelScope.launch {
            _points.value = repository.getListOfSimplePoiAndActivities()
            _isLoading.value = false
        }
    }

    fun refreshList() {
        _isLoading.value = true

        viewModelScope.launch {
            _points.value = repository.getListOfSimplePoiAndActivities()
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