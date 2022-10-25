package com.example.turapp.utils

import com.example.turapp.repository.trackingDb.TrackingDAO
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.MyPointWithGeo
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import org.osmdroid.util.GeoPoint

class MyPointRepository(private val dao: TrackingDAO) {


    suspend fun getMyPointWithGeoPoint(pointId: Int): MyPointWithGeoPoints? {
        val myPoint: MyPoint? = dao.getMyPointById(pointId)

        if (myPoint == null)
            return null

        val geoData = dao.getMyPointsOrderedGeoData(pointId)
        return MyPointWithGeoPoints(
            myPoint, convertToGeopointsWithTime(
                myPoint.createdAt,
                geoData
            )
        )
    }

}

private fun convertToGeopointsWithTime(startTime: Long, list: List<PointGeoData>)
        : List<GeoPointsWithTime> {
    // If empty return nothing
    if (list.isEmpty())
        return mutableListOf()

    val res = mutableListOf<GeoPointsWithTime>()
    list.forEach {
        val lat: Double? = it.lat?.toDouble()
        val long: Double? = it.long?.toDouble()
        val alt: Double? = it.alt?.toDouble()
        val timer = it.timestamp - startTime
        if (lat != null && long != null) {
            res.add(
                GeoPointsWithTime(
                    GeoPoint(lat, long, alt ?: 0.0),
                    timer
                )
            )
        }
    }
    return res
}

data class MyPointWithGeoPoints(
    val point: MyPoint,
    val geo: List<GeoPointsWithTime>
)

data class GeoPointsWithTime(
    val geo: GeoPoint,
    val timeSinceStart: Long
)