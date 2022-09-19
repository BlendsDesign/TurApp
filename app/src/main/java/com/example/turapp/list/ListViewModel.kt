package com.example.turapp.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.startPage.StartViewModel
import java.lang.IllegalArgumentException

class ListViewModel : ViewModel() {


    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ListViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

}