package com.example.turapp.repository.trackingDb

import android.content.Context
import androidx.room.*
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.entities.TempGeoData

@Database(
    entities = [
        MyPoint::class,
        PointGeoData::class,
        TempGeoData::class
    ],
    version = 3,
    autoMigrations = [AutoMigration(
        from = 2,
        to = 3
    )
    ],
    exportSchema = true
)
@TypeConverters(MyConverters::class)
abstract class MyPointDB: RoomDatabase() {
    abstract val myPointDao: MyPointDAO

    companion object {
        @Volatile
        private var INSTANCE: MyPointDB? = null

        fun getInstance(context: Context): MyPointDB {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyPointDB::class.java,
                    "my_point_db"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}