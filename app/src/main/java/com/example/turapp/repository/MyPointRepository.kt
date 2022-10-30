package com.example.turapp.utils

import android.app.Application
import com.example.turapp.repository.trackingDb.MyPointDAO
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo

class MyPointRepository(app: Application) {

    private val dao = MyPointDB.getInstance(app).myPointDao


    suspend fun getAllMyPointsWithGeo(): List<MyPointWithGeo> {
        return dao.getAllMyPointWithGeo()
    }

}
