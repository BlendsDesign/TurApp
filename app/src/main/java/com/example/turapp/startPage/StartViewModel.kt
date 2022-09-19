package com.example.turapp.startPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class StartViewModel(private val testText: String) : ViewModel() {
    // TODO: Implement the ViewModel
    private val _test = MutableLiveData<String>()
    val test: LiveData<String> get() = _test

    init {
        _test.value = testText
    }

    class Factory(val testText: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StartViewModel(testText) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}