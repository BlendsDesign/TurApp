package com.example.turapp.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "activity_lat_lng")
class ActivityGeoData(
    var activityId: Int? = null,
    var lat: Float? = null,
    var lng: Float? = null,
    var altitude: Float? = null,
    var distanceToPrev: Float? = null,
    var personalBestFromPrev: Long? = null,
    var title: String? = null,
    var description: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var geoId: Int? = null
}