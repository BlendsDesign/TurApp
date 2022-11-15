package com.example.turapp.repository.trackingDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.MyPointWeek
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import kotlinx.coroutines.flow.Flow

@Dao
interface MyPointDAO {
    // MyPoint
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyPoint(point: MyPoint): Long

    @Delete
    suspend fun deleteMyPoint(point: MyPoint)

    @Transaction
    @Query("SELECT * FROM my_point ORDER BY createdAt DESC")
    suspend fun getAllMyPointWithGeo(): List<MyPointWithGeo>

    @Transaction
    @Query("SELECT * FROM my_point WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getMyPointByWeeks(startDate: Long, endDate: Long): Flow<List<MyPointWithGeo>>

    @Query("""
        SELECT
           strftime('%W', DATE(DATE(createdAt / 1000, 'unixepoch'), 'weekday 0')) as week,
           MIN(DATE(createdAt / 1000, 'unixepoch')) AS earliest,
           MAX(DATE(createdAt / 1000, 'unixepoch')) AS latest,
           COUNT(*) AS count
        FROM my_point
        GROUP BY DATE(DATE(createdAt / 1000, 'unixepoch'), 'weekday 0')
    """)
    fun getMyPointWeeks(): Flow<List<MyPointWeek>>

    @Transaction
    @Query("SELECT * FROM my_point WHERE pointId = :pointId")
    suspend fun getMyPointById(pointId: Int): MyPointWithGeo?


    // PointGeoData
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyPointGeoData(geoData: PointGeoData): Long

    @Delete
    suspend fun deleteMyPointGeoData(geoData: PointGeoData)

    @Transaction
    @Query("SELECT * FROM geo_data WHERE pointId = :pointId ORDER BY timestamp ASC")
    suspend fun getMyPointsOrderedGeoData(pointId: Int): List<PointGeoData>

    @Transaction
    @Query("SELECT Sum(distanceInMeters) FROM my_point WHERE createdAt BETWEEN :fromDateInMillis AND :toDateInMillis")
    suspend fun getSumDistanceBetweenDates(fromDateInMillis: Long, toDateInMillis: Long): Long
}