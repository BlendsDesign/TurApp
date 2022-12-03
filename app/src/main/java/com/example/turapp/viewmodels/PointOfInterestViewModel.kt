package com.example.turapp.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.turapp.repository.MyPointRepository
import kotlinx.coroutines.launch

class PointOfInterestViewModel(private val app: Application, id: Long) : ViewModel() {


    private val repository = MyPointRepository(app)

    val myPoint = repository.getMyPoint(id).asLiveData()

    val trek = repository.getTrek(id).asLiveData()

    private val _isInEditMode = MutableLiveData<Boolean>()
    val isInEditMode : LiveData<Boolean> get() = _isInEditMode

    fun saveEdits() {
        //TODO Save edits to MyPoint
    }


    private val _finishedDeleting = MutableLiveData<Boolean>()
    val finishedDeleting: LiveData<Boolean> get() = _finishedDeleting

    fun deletePoi() {
        //TODO Delete image as well
        viewModelScope.launch {
            myPoint.value?.let {
                try {
                    it.image?.let {
                        val uri = Uri.parse(it)
                        app.applicationContext.contentResolver.delete(
                            uri, null, null
                        )
                    }
                } catch (e: Exception) {
                    Log.e("PointOfInterestViewModel", "Error deleting image", e)
                }
                _finishedDeleting.value = repository.deleteMyPoint(it, trek.value)
            }
        }
    }


    class Factory(
        private val app: Application,
        private val id: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PointOfInterestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PointOfInterestViewModel(app, id) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}