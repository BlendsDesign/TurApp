package com.example.turapp.utils

import android.app.Application
import com.example.turapp.repository.trackingDb.MyPointDAO
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.PointGeoData
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import org.osmdroid.util.GeoPoint

class MyPointRepository(app: Application) {

    private val dao = MyPointDB.getInstance(app).myPointDao


    suspend fun getAllMyPointsWithGeo(): List<MyPointWithGeo> {
        return dao.getAllMyPointWithGeo()
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
