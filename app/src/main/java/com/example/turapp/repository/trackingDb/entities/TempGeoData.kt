package com.example.turapp.repository.trackingDb.entities

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temp_geo_data")
data class TempGeoData(
    @PrimaryKey(autoGenerate = false)
    var id: Int? = null,
    var loc: Location? = null
)
