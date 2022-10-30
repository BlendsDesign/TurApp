package com.example.turapp.roomDb.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.turapp.roomDb.entities.ActivityGeoData
import com.example.turapp.roomDb.entities.RecordedActivity

data class ActivityWithGeoData(
    @Embedded val activity: RecordedActivity,
    @Relation(
        parentColumn = "activityId",
        entityColumn = "activityId"
    )
    val geoDataSet: List<ActivityGeoData>
)