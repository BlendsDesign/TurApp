package com.example.turapp.repository.trackingDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint

@Entity(tableName = "geo_data")
data class PointGeoData(
    var pointId: Int,
    var timestamp: Long,
    var geoPoint: GeoPoint,
    var type: String? = null,
    var direction: Float? = null
) {
    @PrimaryKey(autoGenerate = true)
    var GeoDataId: Int? = null
}