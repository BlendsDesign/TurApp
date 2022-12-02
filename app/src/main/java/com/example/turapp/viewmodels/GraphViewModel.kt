package com.example.turapp.viewmodels

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.*
import com.example.turapp.repository.trackingDb.entities.MyPointWeek
import com.example.turapp.repository.MyPointRepository
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow

class GraphViewModel(app: Application) : ViewModel() {

    private val repository = MyPointRepository(app)
    val currentWeek: MutableLiveData<MyPointWeek> = MutableLiveData()
    private val channel = Channel<Unit>()

    init {
        currentWeek.observeForever(::updateWeek)
    }

    override fun onCleared() {
        super.onCleared()
        currentWeek.removeObserver(::updateWeek)
    }

    private fun updateWeek(@Suppress("UNUSED_PARAMETER") week: MyPointWeek) {
        viewModelScope.launch { channel.send(Unit) }
    }

    val rawData = liveData {
        channel.receiveAsFlow().cancellable().collectLatest {
            currentWeek.value?.let { week ->
                repository.getAllMyPointsByWeek(week).cancellable().collectLatest { points ->
                    this@liveData.emit(points)
                }
            }
        }

    }

    fun getStepsData() =
        BarData(BarDataSet(rawData.value?.mapIndexed { i, it ->
            BarEntry(i.toFloat(), it.steps?.toFloat() ?: 0f)
        } ?: listOf(), "Steps").apply {
            color = Color.RED
        })

    fun getDistanceData() =
        BarData(BarDataSet(rawData.value?.mapIndexed { i, it ->
            BarEntry(i.toFloat(), it.distanceInMeters ?: 0f)
        } ?: listOf(), "Distance").apply {
            color = Color.BLUE
        })

    fun getTimeData() =
        BarData(BarDataSet(rawData.value?.mapIndexed { i, it ->
            BarEntry(i.toFloat(), ((it.timeTaken?.toFloat() ?: 0f) / 1000) / 60)
        } ?: listOf(), "Time taken").apply {
            color = Color.GREEN
        })


    val weeks = liveData {
        repository.getAllMyPointWeeks().cancellable().collectLatest {
            emit(it)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GraphViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}
