package com.example.turapp.roomDb

import android.location.Location
import com.example.turapp.roomDb.entities.*
import org.osmdroid.util.GeoPoint

class MyRepository(private val poiDao: PoiDao) {

    suspend fun addPoiAndRecordings(poi: PointOfInterest, recs: MutableList<Recording>) {
        val poiId: Int = poiDao.insertPoi(poi).toInt()
        recs.forEach {
            it.poiId = poiId
            poiDao.insertRecording(it)
        }
    }

    suspend fun getAllPoi(): List<PointOfInterest> {
        return poiDao.getAllPois()
    }

    suspend fun addSinglePoi(poi: PointOfInterest) {
        poiDao.insertPoi(poi)
    }

    suspend fun deletePoiAndRecordings(poi: PointOfInterest) {
        poiDao.deleteAssociatedRecordings(poi.poiId?: -1)
        poiDao.deletePoi(poi)
    }

    suspend fun deleteActivityAndGeoData(act: RecordedActivity) {
        val id = act.activityId
        if (id != null) {
            poiDao.deleteAssociatedActivityGeoData(id)
            poiDao.deleteRecordedActivity(act)

        }
    }

    suspend fun insertRecordedActivityAndGeoData(
        recordedActivity: RecordedActivity,
        geo: List<Location>
    ) {
        val activityId: Int = poiDao.insertActivity(recordedActivity).toInt()
        var lastLoc: Location? = null
        var lastTimeStamp: Long? = null
        geo.forEach {
            poiDao.insertActivityGeoData(
                ActivityGeoData(
                    activityId = activityId,
                    lat= it.latitude.toFloat(),
                    lng= it.longitude.toFloat(),
                    altitude = it.altitude.toFloat(),
                    distanceToPrev = it.distanceTo(lastLoc?: it),
                    timeSincePrev = it.time.minus(lastTimeStamp?: it.time)
                )
            )
            lastLoc = it
            lastTimeStamp = it.time
        }
    }

    suspend fun insertActivityGeoData(activityId: Int, loc: Location) {
        if (poiDao.getActivity(activityId).isNotEmpty()) {
            poiDao.insertActivityGeoData(
                ActivityGeoData(activityId, loc.latitude.toFloat(), loc.longitude.toFloat())
            )
        }
    }

    suspend fun getListOfSimplePoiAndActivities(): List<SimplePoiAndActivities> {
        val pois = poiDao.getAllPois()
        val activities = poiDao.getAllActivities()
        val res = mutableListOf<SimplePoiAndActivities>()
        pois.forEach {
            val lat = it.poiLat?.toDouble()
            val lng = it.poiLng?.toDouble()
            val alt = it.poiAltitude?.toDouble()
            var geo: GeoPoint? = null
            if (lat != null && lng != null){
                if (alt != null)
                    geo = GeoPoint(lat, lng, alt)
                else
                    geo = GeoPoint(lat, lng)
            }
            res.add(
                SimplePoiAndActivities(
                    id = it.poiId!!,
                    title = it.poiName,
                    description = it.poiDescription?: "",
                    location = geo,
                    type = TypeOfPoint.POI,
                    timestamp = it.createdAt
                )
            )
        }
        activities.forEach {
            val lat = it.startingLat?.toDouble()
            val lng = it.startingLng?.toDouble()
            val alt = it.startingAltitude?.toDouble()
            var geo: GeoPoint? = null
            if (lat != null && lng != null){
                if (alt != null)
                    geo = GeoPoint(lat, lng, alt)
                else
                    geo = GeoPoint(lat, lng)
            }
            res.add(
                SimplePoiAndActivities(
                    id = it.activityId!!,
                    title = it.title?: "",
                    description = it.description?: "",
                    location = geo,
                    type = TypeOfPoint.RECORDED_ACTIVITY,
                    timestamp = it.timestamp
                )
            )
        }
        return res
    }



}