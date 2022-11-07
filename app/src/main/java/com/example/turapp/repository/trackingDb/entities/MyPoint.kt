package com.example.turapp.repository.trackingDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_point")
data class MyPoint(
    var title: String? = null,
    var description: String? = null,
    var type: String,
    var image: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var distanceInMeters: Float? = null,
    var timeTaken: Long? = null,
    var steps: Long? = null,
    var hasAdditionalData: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var pointId: Int? = null
}
