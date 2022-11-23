package com.example.turapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.repository.MyPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ListViewModel(app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)
    val allPoints = repository.getAllMyPoints().asLiveData()
    private val _points = MutableLiveData<List<MyPoint>>()
    val points: LiveData<List<MyPoint>> get() = _points
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _navigateToSelectedMyPoint = MutableLiveData<MyPoint?>()
    val navigateToSelectedMyPoint: LiveData<MyPoint?> get() = _navigateToSelectedMyPoint
    fun setNavigateToMyPoint(myPoint: MyPoint) {
        _navigateToSelectedMyPoint.value = myPoint
    }

    fun navigateComplete() {
        _navigateToSelectedMyPoint.value = null
    }

    private val _filterBy = MutableLiveData<String?>()
    val filterBy: LiveData<String?> get() = _filterBy

    fun setFilterBy(value: String?) {
        _filterBy.value = when (value) {
            TYPE_POI -> TYPE_POI
            TYPE_TRACKING -> TYPE_TRACKING
            TYPE_SNAPSHOT -> TYPE_SNAPSHOT
            else -> null
        }
        filterList()
    }

    fun filterList() {
        _isLoading.value = true
        if (filterBy.value == null) {
            _points.value = allPoints.value ?: mutableListOf()
        } else {
            val res = mutableListOf<MyPoint>()
            allPoints.value?.forEach {
                if (it.type == filterBy.value)
                    res.add(it)
            }
            _points.value = res
        }
        _isLoading.value = false
    }

    init {
        filterList()
    }


    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ListViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}