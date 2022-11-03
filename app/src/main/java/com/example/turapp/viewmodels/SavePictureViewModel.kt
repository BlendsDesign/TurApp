package com.example.turapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.turapp.repository.MyRepository
import com.example.turapp.repository.trackingDb.MyPointDAO
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import com.example.turapp.roomDb.PoiDatabase
import com.example.turapp.roomDb.SimplePoiAndActivities
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.utils.MyPointRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class SavePictureViewModel(app: Application, private val path: String) : ViewModel() {

    private val repository = MyPointRepository(app)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading

    private val _happyWithPicture = MutableLiveData<Boolean>()
    val happyWithPicture : LiveData<Boolean> get() = _happyWithPicture

    fun setHappyWithPicture() { //switch
        _happyWithPicture.value = _happyWithPicture.value != true
    }


    fun saveMyPoint(title: String?, description: String?) {
        _isLoading.value = true

        viewModelScope.launch {
            val myPointId = repository.insertMyPoint(
                MyPoint(
                    title = title?: path,
                    description = description,
                    image = path,
                    type = TYPE_SNAPSHOT
                )
            )
        }
    }


    class Factory(val app: Application, val picturePath: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SavePictureViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SavePictureViewModel(app, picturePath) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

}