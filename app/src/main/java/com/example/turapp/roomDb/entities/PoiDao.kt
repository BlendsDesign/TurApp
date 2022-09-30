package com.example.turapp.roomDb.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.turapp.roomDb.entities.relations.PoiWithRecordings

@Dao
interface PoiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PointOfInterest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording)

    @Transaction
    @Query("SELECT * FROM point_of_interest WHERE poiId = :poiId")
    suspend fun getPoiWithRecordings(poiId: Int): List<PoiWithRecordings>

    @Delete
    suspend fun deletePoi(poi: PointOfInterest)

    @Delete
    suspend fun deleteRec(rec: Recording)

    @Transaction
    @Query("SELECT * FROM recording")
    suspend fun getAllRecordings(): List<Recording>

    @Transaction
    @Query("SELECT * FROM point_of_interest")
    suspend fun getAllPoiWithRecordings(): List<PoiWithRecordings>

    @Transaction
    @Query("SELECT * FROM point_of_interest")
    suspend fun getAllPointOfInterest(): List<PointOfInterest>

}