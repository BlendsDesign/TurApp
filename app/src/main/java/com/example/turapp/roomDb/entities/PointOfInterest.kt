package com.example.turapp.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_of_interest")
data class PointOfInterest(
    @PrimaryKey(autoGenerate = true)
    val poiId: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val poiLengt: Long,
    val poiName: String = "Unnamed POI",
    val poiLat: Float = -1F,
    val poiLong: Float = -1F,
    val poiDescription: String = ""
)