package com.example.turapp.repository.trackingDb

import android.location.Location
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import java.lang.reflect.Type

class MyConverters {

    @TypeConverter
    fun stringToLocation(data: String?): Location? {
        if (data == null) {
            return null
        }
        val type: Type = object : TypeToken<Location>() {}.type
        return Gson().fromJson<Location>(data, type)
    }
    @TypeConverter
    fun locationToString(data: Location?): String? {
        return Gson().toJson(data)
    }

    @TypeConverter
    fun stringToGeoPoint(data: String?): GeoPoint? {
        if (data == null) {
            return null
        }
        val type: Type = object : TypeToken<GeoPoint>() {}.type
        return Gson().fromJson<GeoPoint>(data, type)
    }
    @TypeConverter
    fun geoPointToString(data: GeoPoint?): String? {
        return Gson().toJson(data)
    }
}