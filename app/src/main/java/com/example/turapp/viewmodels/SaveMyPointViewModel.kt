package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.turapp.repository.MyRepository
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.utils.MyPointRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class SaveMyPointViewModel(private val app: Application): ViewModel() {

    private val repository: MyPointRepository = MyPointRepository(app)

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
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaveMyPointViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SaveMyPointViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}