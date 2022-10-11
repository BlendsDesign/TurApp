package com.example.turapp.roomDb

import android.location.Location
import com.example.turapp.roomDb.entities.*

class MyRepository(private val poiDao: PoiDao) {

    suspend fun addPoiAndRecordings(poi: PointOfInterest, recs: MutableList<Recording>) {
        val poiId: Int = poiDao.insertPoi(poi).toInt()
        recs.forEach {
            it.poiId = poiId
            poiDao.insertRecording(it)
        }
    }

    suspend fun deletePoiAndRecordings(poi: PointOfInterest) {
        poiDao.deleteAssociatedRecordings(poi.poiId?: -1)
        poiDao.deletePoi(poi)
    }

    suspend fun insertRecordedActivityAndGeoData(
        recordedActivity: RecordedActivity,
        geo: List<Location>
    ) {
        val activityId: Int = poiDao.insertActivity(recordedActivity).toInt()
        geo.forEach {
            poiDao.insertActivityGeoData(
                ActivityGeoData(activityId, it.latitude.toFloat(), it.longitude.toFloat())
            )
        }
    }

    suspend fun insertActivityGeoData(activityId: Int, loc: Location) {
        if (poiDao.getActivity(activityId).value != null) {
            poiDao.insertActivityGeoData(
                ActivityGeoData(activityId, loc.latitude.toFloat(), loc.longitude.toFloat())
            )
        }
    }

}