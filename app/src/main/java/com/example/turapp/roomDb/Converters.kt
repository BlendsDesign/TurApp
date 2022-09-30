package com.example.turapp.roomDb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

}