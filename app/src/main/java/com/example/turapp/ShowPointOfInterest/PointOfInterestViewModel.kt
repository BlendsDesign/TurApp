package com.example.turapp.ShowPointOfInterest

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.relations.PoiWithRecordings
import kotlinx.coroutines.launch

class PointOfInterestViewModel(app: Application, poiId: Int): ViewModel() {

    private val _dao: PoiDao = PoiDatabase.getInstance(app).poiDao

    private val _poi = MutableLiveData<List<PoiWithRecordings>>()
    val poi : LiveData<List<PoiWithRecordings>> get() = _poi

    init {
        viewModelScope.launch {
            _poi.value = _dao.getPoiWithRecordings(poiId)
        }
    }


    class Factory(private val app: Application, private val poiId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PointOfInterestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PointOfInterestViewModel(app, poiId) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}