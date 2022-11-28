package com.example.turapp.repository.trackingDb.entities

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint

@Entity(tableName = "trek")
data class TrekLocations(
    @PrimaryKey(autoGenerate = false)
    var myPointId: Long,
    var trekList: MutableList<MutableList<GeoPoint>>
)