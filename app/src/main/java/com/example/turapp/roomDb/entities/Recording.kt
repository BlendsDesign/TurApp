package com.example.turapp.roomDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recording(
    var poiId: Int = -1,
    var sensorType: Int,
    var recording: MutableList<MutableList<Float>>
) {
    @PrimaryKey(autoGenerate = true)
    var recId: Int? = null
}