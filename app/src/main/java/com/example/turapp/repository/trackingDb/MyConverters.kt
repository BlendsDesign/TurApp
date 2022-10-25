package com.example.turapp.repository.trackingDb

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.*

class MyConverters {

    @TypeConverter
    fun stringToList(data: String?): MutableList<MutableList<Float>>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<MutableList<MutableList<Float>>>() {}.type
        return Gson().fromJson<MutableList<MutableList<Float>>>(data, listType)
    }
    @TypeConverter
    fun listToString(data: MutableList<MutableList<Float>>): String? {
        return Gson().toJson(data)
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray?): Bitmap? {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            return null
        }
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap?): ByteArray? {
        if (bmp != null) {
            val outputStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return outputStream.toByteArray()
        }
        else {
            return null
        }
    }
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
}