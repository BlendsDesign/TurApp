package com.example.turapp.roomDb

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.*

class Converters {

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
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}