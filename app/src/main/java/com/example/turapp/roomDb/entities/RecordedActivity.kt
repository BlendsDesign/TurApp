package com.example.turapp.roomDb.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recorded_activity_table")
data class RecordedActivity(
    var image: Bitmap? = null,
    var startingLat: Float? = null,
    var startingLng: Float? = null,
    var startingAltitude: Float? = null,
    var timestamp: Long = 0L,
    var avgSpeed: Float = 0f,
    var totalDistance: Int? = null,
    var timeInMillis: Long = 0L,
    var title: String? = null,
    var description: String? = null,
    var steps: Int? = null
) {
    @PrimaryKey(autoGenerate = true)
    var activityId: Int? = null
}