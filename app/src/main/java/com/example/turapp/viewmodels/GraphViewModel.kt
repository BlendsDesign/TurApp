package com.example.turapp.viewmodels

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.Date

class GraphViewModel : ViewModel() {

    val rawData = MutableLiveData(mutableListOf(
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-01T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1200,
            timeTaken = 30 * 60 * 1000, // milliseconds
            distanceInMeters = 800f,
        ),
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-02T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1300,
            timeTaken = 35 * 60 * 1000, // milliseconds
            distanceInMeters = 900f,
        ),
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-03T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1070,
            timeTaken = 25 * 60 * 1000, // milliseconds
            distanceInMeters = 850f,
        ),
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-04T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1270,
            timeTaken = 37 * 60 * 1000, // milliseconds
            distanceInMeters = 780f,
        ),
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-05T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1200,
            timeTaken = 30 * 60 * 1000, // milliseconds
            distanceInMeters = 800f,
        ),
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-06T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1450,
            timeTaken = 40 * 60 * 1000, // milliseconds
            distanceInMeters = 1000f,
        ),
        MyPoint(
            createdAt = Date.from(Instant.parse("2022-11-07T13:00:00.00Z")).time,
            type = TYPE_TRACKING,
            steps = 1300,
            timeTaken = 38 * 60 * 1000, // milliseconds
            distanceInMeters = 900f,
        ),
    ))

    val data = MutableLiveData(getBarData())


    private fun getBarData() = BarData(
        listOf(
            BarDataSet(rawData.value?.mapIndexed { i, it ->
                BarEntry(i.toFloat(), it.steps?.toFloat() ?: 0f)
            } ?: listOf(), "Steps").apply {
                color = Color.RED
                                          },
            BarDataSet(rawData.value?.mapIndexed { i, it ->
                BarEntry(i.toFloat(), it.distanceInMeters ?: 0f)
            } ?: listOf(), "Distance").apply {
                color = Color.BLUE
                                             },
            BarDataSet(rawData.value?.mapIndexed { i, it ->
                BarEntry(i.toFloat(), ((it.timeTaken?.toFloat() ?: 0f) / 1000) / 60)
            } ?: listOf(), "Time taken").apply {
                color = Color.GREEN
                                               },
        )
    ).apply {
        barWidth = 0.30f
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GraphViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}