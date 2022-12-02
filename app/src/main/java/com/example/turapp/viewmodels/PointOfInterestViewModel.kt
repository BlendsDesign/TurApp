package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.repository.MyPointRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class PointOfInterestViewModel(app: Application, private val id: Long, val typeArgument: String) : ViewModel() {


    private val repository = MyPointRepository(app)

    val myPoint = repository.getMyPoint(id).asLiveData()

    val trek = repository.getTrek(id).asLiveData()

    private val _isInEditMode = MutableLiveData<Boolean>()
    val isInEditMode : LiveData<Boolean> get() = _isInEditMode

    fun saveEdits() {
        //TODO Save edits to MyPoint
    }

    private val _loadingImage = MutableLiveData<Boolean>()
    val loadingImage: LiveData<Boolean> get() = _loadingImage


    private val _finishedDeleting = MutableLiveData<Boolean>()
    val finishedDeleting: LiveData<Boolean> get() = _finishedDeleting

    fun deletePoi() {
        //TODO Delete image as well
        _loadingImage.value = true
        viewModelScope.launch {
            myPoint.value?.let {
                _finishedDeleting.value = repository.deleteMyPoint(it, trek.value)
            }
        }
    }


    class Factory(
        private val app: Application,
        private val id: Long,
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