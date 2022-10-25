package com.example.turapp.repository.trackingDb.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MyPointWithGeo(
    @Embedded val point: MyPoint,
    @Relation(
        parentColumn = "pointId",
        entityColumn = "pointId"
    )
    val geoData: List<PointGeoData>
)
