package com.example.turapp.ShowPointOfInterest

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.relations.PoiWithRecordings
import kotlinx.coroutines.launch

class PointOfInterestViewModel(app: Application, poiId: Int): ViewModel() {

    private val _dao: PoiDao = PoiDatabase.getInstance(app).poiDao

    private val _poi = MutableLiveData<PoiWithRecordings>()
    val poi : LiveData<PoiWithRecordings> get() = _poi
    private val _loadingImage = MutableLiveData<Boolean>()
    val loadingImage : LiveData<Boolean> get() = _loadingImage

    init {
        viewModelScope.launch {
            _loadingImage.value = true
            val poi = _dao.getPoiWithRecordings(poiId)
            if (poi.size > 0) {
                _poi.value = poi[0]
            }
            _loadingImage.value = false
        }
    }

    private val _finishedDeleting = MutableLiveData<Boolean>()
    val finishedDeleting: LiveData<Boolean> get() = _finishedDeleting
    fun deletePoi() {
        _loadingImage.value = true
        viewModelScope.launch {
            val poi: PoiWithRecordings? = _poi.value
            if (poi != null) {
                poi.recording.forEach {
                    _dao.deleteRec(it)
                }
                _dao.deletePoi(poi.poi)
                _finishedDeleting.value = true
            }
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