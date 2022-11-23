package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.MyPointRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class StartViewModel(app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)
    private val _points = MutableLiveData<List<MyPoint>>()
    val points : LiveData<List<MyPoint>> get() = _points
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading


    fun refreshList() {
        _isLoading.value = true

        viewModelScope.launch {
            repository.getAllMyPoints().collect{
                _points.value = it
            }
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