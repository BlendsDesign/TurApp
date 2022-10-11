package com.example.turapp.roomDb

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.turapp.roomDb.entities.*

@Database(
    entities = [
        PointOfInterest::class,
        Recording::class,
        RecordedActivity::class,
        ActivityGeoData::class
    ],
    autoMigrations = [AutoMigration(from = 3, to = 4) ],
    version = 4,
    exportSchema = true
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
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}