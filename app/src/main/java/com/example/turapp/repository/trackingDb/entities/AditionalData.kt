package com.example.turapp.repository.trackingDb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "additional_data")
data class AditionalData(
    var pointId: Int,
    var dataType: String,
    var data: String? = null,
    var numberData: List<Float>? = null
) {
    @PrimaryKey(autoGenerate = true)
    var additionalDataId: Int? = null
}
