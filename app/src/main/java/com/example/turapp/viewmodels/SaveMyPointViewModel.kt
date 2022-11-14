package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.utils.MyPointRepository
import com.example.turapp.utils.helperFiles.NAVIGATION_ARGUMENT_SAVING_TYPE_TRACKING
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class SaveMyPointViewModel(private val app: Application, private val typeArgument: String): ViewModel() {

    private val repository: MyPointRepository = MyPointRepository(app)

    private val _finishedSavingPoint = MutableLiveData<Boolean>()
    val finishedSavingPoint: LiveData<Boolean> get() = _finishedSavingPoint

    private val _trackedLocations = MutableLiveData<MutableList<MutableList<GeoPoint>>?>()
    val trackedLocations: LiveData<MutableList<MutableList<GeoPoint>>?> get() = _trackedLocations

    init {
        if (typeArgument == NAVIGATION_ARGUMENT_SAVING_TYPE_TRACKING) {
            val tempTracked = NowTrackingViewModel.getTreck()
            _trackedLocations.value = tempTracked
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
                    type = TYPE_POI,
                    geoList = geoList,
                    adress = marker.title
                )
                _finishedSavingPoint.value = true
            }
        }
    }

    class Factory(private val app: Application, private val typeArgument: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaveMyPointViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SaveMyPointViewModel(app, typeArgument) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}