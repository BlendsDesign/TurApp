package com.example.turapp.repository.trackingDb.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_point")
data class MyPoint(
    var title: String? = null,
    var desc: String? = null,
    var type: String,
    var image: Bitmap? = null,
    var totalDistance: Float? = null,
    var hasAdditionalData: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var pointId: Int? = null
    val createdAt: Long = System.currentTimeMillis()
}
