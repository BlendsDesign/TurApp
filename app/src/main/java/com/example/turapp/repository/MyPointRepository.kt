package com.example.turapp.utils

import android.app.Application
import android.net.Uri
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.MyPointWeek
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit

class MyPointRepository(app: Application) {

    private val dao = MyPointDB.getInstance(app).myPointDao


    suspend fun getAllMyPointsWithGeo(): List<MyPointWithGeo> {
        return dao.getAllMyPointWithGeo()
    }

    fun getAllMyPointsByWeek(week: MyPointWeek): Flow<List<MyPointWithGeo>> {
        return dao.getMyPointByWeeks(
            Instant.parse("${week.earliest}T00:00:00.00Z").toEpochMilli(),
            Instant.parse("${week.latest}T00:00:00.00Z")
                .plus(1, ChronoUnit.DAYS)
                .minusMillis(1)
                .toEpochMilli(),
        )
    }

    fun getAllMyPointWeeks(): Flow<List<MyPointWeek>> {
        return dao.getMyPointWeeks()
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


    suspend fun getSumRanLastSevenDays(): Long {
        val currentTime = System.currentTimeMillis()
        val firstTime = 100L
        return dao.getSumDistanceBetweenDates(firstTime, currentTime)
    }

    suspend fun createMyPointWithGeo(
        imageUri: Uri?,
        title: String,
        desc: String?,
        type: String,
        adress: String?,
        geoList: List<PointGeoData>
    ): Boolean {
        var uriString : String? = null
        if (imageUri != null)
            uriString = imageUri.toString()

        val myPointId = dao.insertMyPoint(
            MyPoint(
                image = uriString,
                title = title,
                description = desc,
                adress = adress,
                type = type
            )
        )
        geoList.forEach {
            it.pointId = myPointId.toInt()
            dao.insertMyPointGeoData(it)
        }
        return true
    }

    suspend fun insertMyPoint(myPoint: MyPoint /*, geo: GeoPoint, timestamp: Long*/) : Int {
        val pointId =  dao.insertMyPoint(myPoint).toInt()

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
