package com.example.turapp.repository.trackingDb.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TrekLocations

data class MyPointWithTrek(
    @Embedded val point: MyPoint,
    @Relation(
        parentColumn = "pointId",
        entityColumn = "myPointId"
    )
    val trec: TrekLocations
)
