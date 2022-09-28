package com.example.turapp.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_of_interest")
data class PointOfInterest(
    @PrimaryKey(autoGenerate = true)
    val poiId: Int = 0,
    val poiTime: String,
    val poiLengt: Float,
    val poiName: String,
    val poiLat: Float,
    val poiLong: Float
)