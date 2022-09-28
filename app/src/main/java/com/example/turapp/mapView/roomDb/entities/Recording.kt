package com.example.turapp.mapView.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val recId: Int = 0,
    val poiId: Int,
    val sensorType: Int,
    val recording: String
)