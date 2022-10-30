package com.example.turapp.repository.trackingDb.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.PointGeoData

data class MyPointWithGeo(
    @Embedded val point: MyPoint,
    @Relation(
        parentColumn = "pointId",
        entityColumn = "pointId"
    )
    val geoData: List<PointGeoData>
)
