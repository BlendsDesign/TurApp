package com.example.turapp.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.MyPointRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class SaveMyPointViewModel(private val app: Application, private val typeArgument: String, uri: Uri?): ViewModel() {

    private val repository: MyPointRepository = MyPointRepository(app)

    private val _finishedSavingPoint = MutableLiveData<Boolean>()
    val finishedSavingPoint: LiveData<Boolean> get() = _finishedSavingPoint

    private val _trackedLocations = MutableLiveData<MutableList<MutableList<GeoPoint>>?>()
    val trackedLocations: LiveData<MutableList<MutableList<GeoPoint>>?> get() = _trackedLocations

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> get() = _imageUri

    init {
        if (typeArgument == TYPE_TRACKING) {
            val tempTracked = NowTrackingViewModel.getTreck()
            _trackedLocations.value = tempTracked
        }
        if(uri != null) {
            _imageUri.value = uri!!
        }
    }

    fun saveSinglePoint(title: String, description: String, marker: Marker?) {
        viewModelScope.launch {
            val geoList = mutableListOf<PointGeoData>()
            if (marker != null) {
                val geo = PointGeoData(-1, System.currentTimeMillis(), marker.position)
                geoList.add(geo)
                repository.createMyPointWithGeo(
                    title = title,
                    desc = description,
                    type = typeArgument,
                    geoList = geoList,
                    adress = marker.title
                )
                _finishedSavingPoint.value = true
            }
        }
    }

    class Factory(private val app: Application, private val typeArgument: String, private val uri: Uri?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaveMyPointViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SaveMyPointViewModel(app, typeArgument, uri) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}