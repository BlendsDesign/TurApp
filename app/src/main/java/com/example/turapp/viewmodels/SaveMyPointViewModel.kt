package com.example.turapp.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.repository.MyPointRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class SaveMyPointViewModel(private val app: Application, val typeArgument: String, uri: Uri?) :
    ViewModel() {

    private val repository: MyPointRepository = MyPointRepository(app)

    private val _finishedSavingPoint = MutableLiveData<Boolean>()
    val finishedSavingPoint: LiveData<Boolean> get() = _finishedSavingPoint

    private val _trackedLocations = MutableLiveData<MutableList<MutableList<GeoPoint>>>()
    val trackedLocations: LiveData<MutableList<MutableList<GeoPoint>>> get() = _trackedLocations

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> get() = _imageUri

    private var _timeOfTrekInMillis: Long? = null
    private var _distanceOfTrek: Float? = null
    private var _steps: Int? = null
    private var _totalAscent : Float? = null

    init {
        if (typeArgument == TYPE_TRACKING) {
            val tempTracked = NowTrackingViewModel.getTreck()
            NowTrackingViewModel.apply {
                getTimeInHundreds()?.let {
                    _timeOfTrekInMillis = it * 10
                }
                _distanceOfTrek = getDistance()
                _steps = getSteps()
                _totalAscent = getTotalAscent()
            }
            if (tempTracked != null) {
                _trackedLocations.value = tempTracked!!
            }
        }

        uri?.let {
            _imageUri.value = it
        }
    }

    fun saveSinglePoint(title: String, description: String, marker: Marker?) {
        viewModelScope.launch {
            var image: String? = null
            _imageUri.value?.let {
                image = it.toString()
            }

            val myPoint = MyPoint(
                image = image,
                type = typeArgument,
                title = title,
                description = description,
                timeTaken = _timeOfTrekInMillis,
                location = marker?.position,
                distanceInMeters = _distanceOfTrek,
                steps = _steps?.toLong(),
                totalAscent = _totalAscent
            )
            var geoList: MutableList<MutableList<GeoPoint>>? = null
            if (typeArgument == TYPE_TRACKING) {
                _trackedLocations.value?.let {
                    geoList = it
                }
            }

            _finishedSavingPoint.value = repository.insertMyPoint(myPoint, geoList)
        }
    }

    fun deleteTakenPicture() {
        try {
            _imageUri.value?.let {
                app.applicationContext.contentResolver.delete(
                    it, null, null
                )
            }
        } catch (e: Exception) {
            Log.e("SelfieViewModel", "Error deleting image", e)
        }
    }

    class Factory(
        private val app: Application,
        private val typeArgument: String,
        private val uri: Uri?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaveMyPointViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SaveMyPointViewModel(app, typeArgument, uri) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}