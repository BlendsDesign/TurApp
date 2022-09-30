package com.example.turapp.roomDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.turapp.roomDb.entities.PoiDao
import com.example.turapp.roomDb.entities.PointOfInterest
import com.example.turapp.roomDb.entities.Recording

@Database(
    entities = [
        PointOfInterest::class,
        Recording::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class PoiDatabase: RoomDatabase() {

    abstract val poiDao: PoiDao

    companion object {
        @Volatile
        private var INSTANCE: PoiDatabase? = null

        fun getInstance(context: Context): PoiDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PoiDatabase::class.java,
                    "poi_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}