package com.example.turapp.startPage

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.roomDb.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.PointOfInterest
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class StartViewModel(app: Application) : ViewModel() {

    private val dao : PoiDao = PoiDatabase.getInstance(app).poiDao
    private val repository = MyRepository(dao)
    private val _points = MutableLiveData<List<PointOfInterest>>()
    val points : LiveData<List<PointOfInterest>> get() = _points
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading


    init {
        _isLoading.value = true

        viewModelScope.launch {
            _points.value = dao.getAllPointOfInterest()
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