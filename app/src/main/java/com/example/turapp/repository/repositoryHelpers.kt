package com.example.turapp.roomDb

import org.osmdroid.util.GeoPoint

enum class TypeOfPoint {
    POI, RECORDED_ACTIVITY
}

data class SimplePoiAndActivities(
    val id: Int,
    val title: String,
    val description: String,
    val location: GeoPoint?,
    val type: TypeOfPoint,
    val timestamp: Long
)