package com.example.turapp.roomDb.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recorded_activity_table")
data class RecordedActivity(
    var image: Bitmap? = null,
    var startingLat: Float = 0F,
    var startingLng: Float = 0F,
    var timestamp: Long = 0L,
    var avgSpeed: Float = 0f,
    var distance: Int = 0,
    var timeInMillis: Long = 0L,
) {
    @PrimaryKey(autoGenerate = true)
    var activityId: Int? = null
}