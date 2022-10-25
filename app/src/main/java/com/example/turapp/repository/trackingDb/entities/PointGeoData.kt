package com.example.turapp.repository.trackingDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_data")
data class PointGeoData(
    var pointId: Int,
    var timestamp: Long,
    var lat: Float? = null,
    var long: Float? = null,
    var alt: Float? = null,
    var type: String?
) {
    @PrimaryKey(autoGenerate = true)
    var GeoDataId: Int? = null
}
