package com.example.turapp.utils

import android.app.Application
import android.net.Uri
import com.example.turapp.repository.trackingDb.MyPointDAO
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.*
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import org.osmdroid.util.GeoPoint

class MyPointRepository(app: Application) {

    private val dao = MyPointDB.getInstance(app).myPointDao


    suspend fun getAllMyPointsWithGeo(): List<MyPointWithGeo> {
        return dao.getAllMyPointWithGeo()
    }

    suspend fun getMyPointWithGeo(id: Int): MyPointWithGeo? {
        return dao.getMyPointById(id)
    }

    suspend fun deleteMyPointWithGeo(point: MyPointWithGeo): Boolean {
        return try {
            point.geoData.forEach {
                dao.deleteMyPointGeoData(it)
            }
            dao.deleteMyPoint(point.point)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun limitPoints(limit:Int) {
        dao.limitPoints(limit)
    }

    suspend fun getSumRanLastSevenDays(): Long {
        val currentTime = System.currentTimeMillis()
        val firstTime = 100L
        return dao.getSumDistanceBetweenDates(firstTime, currentTime)
    }

    suspend fun saveMyPointWithGeo(
        myPoint: MyPoint,
        geoList: MutableList<MutableList<GeoPoint>>
    ): Boolean {
        if (myPoint.type != TYPE_TRACKING) {
            val pointId = dao.insertMyPoint(myPoint)
            if (geoList.isEmpty() || geoList.first().isEmpty())
                return true
            else {
                val geoPoint = geoList.first().first()
                dao.insertMyPointGeoData(
                    PointGeoData(
                        pointId = pointId.toInt(),
                        timestamp = myPoint.createdAt,
                        geoPoint = geoPoint,
                        type = myPoint.type
                    )
                )
                return true
            }
        } else {
            if (geoList.isEmpty() || geoList.first().isEmpty())
                return false
            var totalDistance: Double = 0.0
            val listPointGeoData = mutableListOf<PointGeoData>()
            var isResumingPoint: Boolean = false
            listPointGeoData.add(PointGeoData(-1, myPoint.createdAt, geoList.first().first(), type = TRACKING_STARTING_POINT))
            geoList.forEach { outerlist ->

                var previous: GeoPoint? = null
                // Iterating through the list
                val it = outerlist.iterator()
                while (it.hasNext()) {
                    var point = it.next()
                    previous?.let { prev ->
                        totalDistance += prev.distanceToAsDouble(point)
                    }
                    previous = point
                    if (!it.hasNext()) {
                        listPointGeoData.add(
                            PointGeoData(
                                pointId = -1,
                                timestamp = myPoint.createdAt,
                                geoPoint = point,
                                type = TRACKING_PAUSE_POINT
                            )
                        )
                        isResumingPoint = true
                    } else if (isResumingPoint) {
                        listPointGeoData.add(
                            PointGeoData(
                                pointId = -1,
                                timestamp = myPoint.createdAt,
                                geoPoint = point,
                                type = TRACKING_RESUME_POINT
                            )
                        )
                        isResumingPoint = false
                    } else {
                        listPointGeoData.add(
                            PointGeoData(
                                pointId = -1,
                                timestamp = myPoint.createdAt,
                                geoPoint = point
                            ))
                    }
                }
            }
            listPointGeoData.first().type = TRACKING_STARTING_POINT
            listPointGeoData.last().type = TRACKING_END_POINT
            myPoint.distanceInMeters = totalDistance.toFloat()
            val pointId = dao.insertMyPoint(myPoint)
            listPointGeoData.forEach {
                it.pointId = pointId.toInt()
                dao.insertMyPointGeoData(it)
            }
            return true
        }
    }

    suspend fun insertMyPoint(myPoint: MyPoint /*, geo: GeoPoint, timestamp: Long*/): Int {
        val pointId = dao.insertMyPoint(myPoint).toInt()

        /*val pointGeoData = PointGeoData(
            pointId = pointId,
            timestamp = timestamp,
            geoPoint = geo
        )

        dao.insertMyPointGeoData(pointGeoData)
        */
        return pointId
    }

}
