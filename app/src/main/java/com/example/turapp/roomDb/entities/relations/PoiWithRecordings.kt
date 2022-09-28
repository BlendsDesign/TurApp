package com.example.turapp.roomDb.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.turapp.roomDb.entities.PointOfInterest
import com.example.turapp.roomDb.entities.Recording

data class PoiWithRecordings(
    @Embedded val poi: PointOfInterest,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val recording: List<Recording>
)