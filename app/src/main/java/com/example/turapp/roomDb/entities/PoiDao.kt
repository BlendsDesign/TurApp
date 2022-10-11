package com.example.turapp.roomDb.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.turapp.roomDb.entities.relations.ActivityWithGeoData
import com.example.turapp.roomDb.entities.relations.PoiWithRecordings

@Dao
interface PoiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PointOfInterest): Long

    @Delete
    suspend fun deletePoi(poi: PointOfInterest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording)

    @Delete
    suspend fun deleteRec(rec: Recording)

    @Query("DELETE FROM recording WHERE poiId = :poiId")
    suspend fun deleteAssociatedRecordings(poiId: Int)

    @Transaction
    @Query("SELECT * FROM point_of_interest WHERE poiId = :poiId")
    suspend fun getPoiWithRecordings(poiId: Int): List<PoiWithRecordings>

    @Transaction
    @Query("SELECT * FROM recording")
    suspend fun getAllRecordings(): List<Recording>

    @Transaction
    @Query("SELECT * FROM point_of_interest")
    suspend fun getAllPoiWithRecordings(): List<PoiWithRecordings>

    @Transaction
    @Query("SELECT * FROM point_of_interest WHERE (poiLat BETWEEN :minLat AND :maxLat) AND (poiLng BETWEEN :minLng AND :maxLng)")
    fun loadAllPoiWithinRange(minLat: Float, maxLat: Float, minLng: Float, maxLng: Float): List<PoiWithRecordings>

    @Transaction
    @Query("SELECT * FROM point_of_interest")
    suspend fun getAllPointOfInterest(): List<PointOfInterest>


    // ACTIVITY
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: RecordedActivity): Long

    @Delete
    suspend fun deleteRecordedActivity(recordedActivity: RecordedActivity)

    @Query("SELECT * FROM recorded_activity_table WHERE activityId = :activityId")
    fun getActivity(activityId: Int): LiveData<RecordedActivity>

    @Transaction
    @Query("SELECT * FROM recorded_activity_table WHERE activityId = :activityId")
    fun getActivityWithGeoData(activityId: Int): LiveData<List<ActivityWithGeoData>>

    @Transaction
    @Query("SELECT * FROM recorded_activity_table WHERE (startingLat BETWEEN :minLat AND :maxLat) AND (startingLng BETWEEN :minLng AND :maxLng)")
    fun loadAllActivityWithinRange(minLat: Float, maxLat: Float, minLng: Float, maxLng: Float): LiveData<List<PoiWithRecordings>>

    @Transaction
    @Query("SELECT * FROM recorded_activity_table ORDER BY distance DESC")
    fun getAllActivityOrderedByDistance(): LiveData<List<ActivityWithGeoData>>

    @Transaction
    @Query("SELECT * FROM recorded_activity_table ORDER BY avgSpeed DESC")
    fun getAllActivityOrderedByAvgSpeed(): LiveData<List<ActivityWithGeoData>>

    @Transaction
    @Query("SELECT * FROM recorded_activity_table ORDER BY timestamp DESC")
    fun getAllActivityOrderedByDate(): LiveData<List<ActivityWithGeoData>>


    // ACTIVITY TOTALS
    @Query("SELECT SUM(timeInMillis) FROM recorded_activity_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(distance) FROM recorded_activity_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeed) FROM recorded_activity_table")
    fun getAvgSpeed(): LiveData<Float>

    @Query("SELECT AVG(distance) FROM recorded_activity_table")
    fun getAvgDistance(): LiveData<Int>

    @Query("SELECT AVG(timeInMillis) FROM recorded_activity_table")
    fun getAvgTimeTaken(): LiveData<Long>



    // GEODATA
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityGeoData(activityGeoData: ActivityGeoData)

    @Delete
    suspend fun deleteActivityGeoData(activityGeoData: ActivityGeoData)

    @Query("DELETE FROM activity_lat_lng WHERE activityId = :activityId")
    suspend fun deleteAssociatedGeoData(activityId: Int)


}