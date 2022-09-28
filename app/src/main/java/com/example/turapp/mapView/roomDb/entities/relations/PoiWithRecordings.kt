package com.example.turapp.mapView.roomDb.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.turapp.mapView.roomDb.entities.PointOfInterest
import com.example.turapp.mapView.roomDb.entities.Recording

data class PoiWithRecordings(
    @Embedded val poi: PointOfInterest,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val recording: List<Recording>
)