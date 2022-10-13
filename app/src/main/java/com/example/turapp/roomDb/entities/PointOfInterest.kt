package com.example.turapp.roomDb.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_of_interest")
data class PointOfInterest(
    val createdAt: Long = System.currentTimeMillis(),
    var image: Bitmap? = null,
    var poiLengt: Long? = null,
    var poiName: String = "Unnamed POI",
    var poiLat: Float? = null,
    var poiLng: Float? = null,
    var poiDescription: String? = null
) {
    @PrimaryKey(autoGenerate = true)
    var poiId: Int? = null
}