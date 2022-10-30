package com.example.turapp.utils

import com.example.turapp.repository.trackingDb.MyPointDAO
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo

class MyPointRepository(private val dao: MyPointDAO) {


    suspend fun getAllMyPointsWithGeo(): List<MyPointWithGeo> {
        return dao.getAllMyPointWithGeo()
    }

}
