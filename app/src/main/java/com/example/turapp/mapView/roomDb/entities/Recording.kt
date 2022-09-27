package com.example.turapp.mapView.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val recId: Int,
    val poiId: Int,
    val sensorType: Int,
    val recording: String
)