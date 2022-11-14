package com.example.turapp.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.turapp.repository.MyRepository
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.TypeOfPoint
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.relations.ActivityWithGeoData
import com.example.turapp.roomDb.entities.relations.PoiWithRecordings
import com.example.turapp.utils.MyPointRepository
import kotlinx.coroutines.launch

class PointOfInterestViewModel(app: Application, id: Int, val typeArgument: String) : ViewModel() {


    private val repository = MyPointRepository(app)

    private val _myPoint = MutableLiveData<MyPointWithGeo>()
    val myPoint: LiveData<MyPointWithGeo> get() = _myPoint

    private val _isInEditMode = MutableLiveData<Boolean>()
    val isInEditMode : LiveData<Boolean> get() = _isInEditMode

    fun saveEdits() {
        //TODO Save edits to MyPoint
    }

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> get() = _imageUri

    private val _loadingImage = MutableLiveData<Boolean>()
    val loadingImage: LiveData<Boolean> get() = _loadingImage

    init {
        viewModelScope.launch {
            _loadingImage.value = true
            val temp = repository.getMyPointWithGeo(id)
            if (temp != null) {
                _myPoint.value = temp!!
                temp?.point?.image?.let {
                    _imageUri.value = Uri.parse(it)
                }
            }
            _loadingImage.value = false

        }
    }

    private val _finishedDeleting = MutableLiveData<Boolean>()
    val finishedDeleting: LiveData<Boolean> get() = _finishedDeleting

    fun deletePoi() {
        _loadingImage.value = true
        viewModelScope.launch {
            _myPoint.value?.let {
                _finishedDeleting.value = repository.deleteMyPointWithGeo(it)
            }
        }
    }


    class Factory(
        private val app: Application,
        private val id: Int,
        private val typeArgument: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PointOfInterestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PointOfInterestViewModel(app, id, typeArgument) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}