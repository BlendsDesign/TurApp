package com.example.turapp

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModel(private val prefs: SharedPreferences): ViewModel() {
    private val editor = prefs.edit()
    private val _isNightMode = MutableLiveData<Boolean>()
    val isNightMode : LiveData<Boolean> get() = _isNightMode

    init {
        _isNightMode.value = prefs.getBoolean("isNight", false)
    }

    fun setIsNight() {
        if(_isNightMode.value!!) {
            _isNightMode.value = false
            editor.apply {
                putBoolean("isNight", false)
                apply()
            }
        } else {
            _isNightMode.value = true
            editor.apply {
                putBoolean("isNight", true)
                apply()
            }
        }
    }


    class Factory(private val prefs: SharedPreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(prefs) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}