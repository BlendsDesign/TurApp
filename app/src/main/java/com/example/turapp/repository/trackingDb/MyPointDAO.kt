package com.example.turapp.repository.trackingDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TrekLocations
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
    fun getAllMyPoints(): Flow<MutableList<MyPoint>>

    @Transaction
    @Query("SELECT * FROM my_point WHERE pointId = :pointId")
    fun getMyPointById(pointId: Long): Flow<MyPoint>


    // TrekLocations
    @Transaction
    @Query("SELECT * FROM trek WHERE myPointId= :pointId")
    fun getTrekById(pointId: Long): Flow<TrekLocations>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrek(trek: TrekLocations): Long

    @Delete
    suspend fun deleteTrekLocations(trek: TrekLocations)

    @Transaction
    @Query("SELECT Sum(distanceInMeters) FROM my_point WHERE createdAt BETWEEN :fromDateInMillis AND :toDateInMillis")
    suspend fun getSumDistanceBetweenDates(fromDateInMillis: Long, toDateInMillis: Long): Long

    @Transaction
    @Query("SELECT * FROM my_point ORDER BY createdAt DESC LIMIT :limit ")
    suspend fun limitPoints(limit: Int): List<MyPointWithGeo>


}