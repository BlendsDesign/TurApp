package com.example.turapp.roomDb

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.example.turapp.roomDb.entities.*

@Database(
    entities = [
        PointOfInterest::class,
        Recording::class,
        RecordedActivity::class,
        ActivityGeoData::class
    ],
    version = 11,
    autoMigrations = [AutoMigration(
        from = 9,
        to = 10,
        spec = PoiDatabase.MyExampleAutoMigration::class
    ), AutoMigration(from = 10, to = 11, spec = PoiDatabase.Migration10To11::class)
                     ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PoiDatabase: RoomDatabase() {

    abstract val poiDao: PoiDao
    @RenameColumn(
        tableName = "recorded_activity_table",
        fromColumnName = "distance",
        toColumnName = "totalDistance")
    class MyExampleAutoMigration : AutoMigrationSpec {}
    @RenameColumn(
        tableName = "activity_lat_lng",
        fromColumnName = "personalBestFromPrev",
        toColumnName = "timeSincePrev")
    class Migration10To11 : AutoMigrationSpec {}



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