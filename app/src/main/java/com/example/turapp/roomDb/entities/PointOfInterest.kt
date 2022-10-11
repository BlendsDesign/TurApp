package com.example.turapp.roomDb.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_of_interest")
data class PointOfInterest(
    val createdAt: Long = System.currentTimeMillis(),
    var image: Bitmap? = null,
    var poiLengt: Long = 0,
    var poiName: String = "Unnamed POI",
    var poiLat: Float = -1F,
    var poiLng: Float = -1F,
    var poiDescription: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var poiId: Int? = null
}