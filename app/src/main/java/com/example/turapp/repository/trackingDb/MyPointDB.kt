package com.example.turapp.repository.trackingDb

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TrekLocations

@Database(
    entities = [
        MyPoint::class,
        TrekLocations::class,
    ],
    version = 6,
    autoMigrations = [AutoMigration(
        from = 4,
        to = 5,
        spec = MyPointDB.Migration4To5::class),
        AutoMigration(
            from = 5,
            to = 6)
    ],
    exportSchema = true,
)
@TypeConverters(MyConverters::class)
abstract class MyPointDB: RoomDatabase() {
    abstract val myPointDao: MyPointDAO


    @DeleteTable(tableName = "geo_data")
    @DeleteTable(tableName = "temp_geo_data")
    class Migration4To5 : AutoMigrationSpec {}

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