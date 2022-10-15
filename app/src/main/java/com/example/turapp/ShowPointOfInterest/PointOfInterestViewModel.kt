package com.example.turapp.ShowPointOfInterest

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.MyRepository
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.TypeOfPoint
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.relations.ActivityWithGeoData
import com.example.turapp.roomDb.entities.relations.PoiWithRecordings
import kotlinx.coroutines.launch

class PointOfInterestViewModel(app: Application, id: Int, val type: TypeOfPoint) : ViewModel() {

    private val _dao: PoiDao = PoiDatabase.getInstance(app).poiDao
    private val repository = MyRepository(_dao)

    private val _poi = MutableLiveData<PoiWithRecordings>()
    val poi: LiveData<PoiWithRecordings> get() = _poi
    private val _activity = MutableLiveData<ActivityWithGeoData>()
    val activity : LiveData<ActivityWithGeoData> get() = _activity

    private val _loadingImage = MutableLiveData<Boolean>()
    val loadingImage: LiveData<Boolean> get() = _loadingImage

    init {
        viewModelScope.launch {
            _loadingImage.value = true
            if (type == TypeOfPoint.POI) {
                val poi = _dao.getPoiWithRecordings(id)
                if (poi.size > 0) {
                    _poi.value = poi[0]
                }
            } else if(type == TypeOfPoint.RECORDED_ACTIVITY) {
                val temp = _dao.getActivityWithGeoData(id)
                if(temp.isNotEmpty())
                    _activity.value = temp[0]
            }
            _loadingImage.value = false
        }
    }

    private val _finishedDeleting = MutableLiveData<Boolean>()
    val finishedDeleting: LiveData<Boolean> get() = _finishedDeleting
    fun deletePoi() {
        _loadingImage.value = true
        viewModelScope.launch {
            if (type == TypeOfPoint.POI) {
                val poi: PoiWithRecordings? = _poi.value
                if (poi != null) {
                    repository.deletePoiAndRecordings(poi.poi)
                }
            } else {
                val act = _activity.value
                if (act != null) {
                    repository.deleteActivityAndGeoData(act.activity)
                }
            }
        }
    }


    class Factory(
        private val app: Application,
        private val id: Int,
        private val type: TypeOfPoint
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PointOfInterestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PointOfInterestViewModel(app, id, type) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}