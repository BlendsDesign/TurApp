package com.example.turapp.repository.trackingDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo

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
    @Query("SELECT * FROM my_point WHERE pointId = :pointId")
    suspend fun getMyPointById(pointId: Int): MyPoint


    // PointGeoData
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyPoint(geoData: PointGeoData): Long

    @Delete
    suspend fun deleteMyPoint(geoData: PointGeoData)

    @Transaction
    @Query("SELECT * FROM geo_data WHERE pointId = :pointId ORDER BY timestamp ASC")
    suspend fun getMyPointsOrderedGeoData(pointId: Int): List<PointGeoData>
}