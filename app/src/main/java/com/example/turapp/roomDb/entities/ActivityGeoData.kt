package com.example.turapp.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "activity_lat_lng")
class ActivityGeoData(
    var activityId: Int = 0,
    var lat: Float = 0f,
    var lng: Float = 0f,
    var distanceToPrev: Float? = null,
    var title: String? = null,
    var description: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var geoId: Int? = null
}