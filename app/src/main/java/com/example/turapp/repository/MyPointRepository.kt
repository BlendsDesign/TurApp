package com.example.turapp.utils

import com.example.turapp.repository.trackingDb.TrackingDAO
import com.example.turapp.repository.trackingDb.entities.MyPoint

class MyPointRepository(private val dao: TrackingDAO) {


    suspend fun getMyPointWithGeoPoint(pointId: Int) {
        val myPoint: MyPoint? = dao.getMyPointById(pointId)

        if (myPoint == null) {
            //TODO DO STUFF HERE

        }
    }

}
