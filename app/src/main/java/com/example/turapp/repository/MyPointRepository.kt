package com.example.turapp.repository

import android.app.Application
import com.example.turapp.repository.trackingDb.MyPointDB
import com.example.turapp.repository.trackingDb.entities.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.osmdroid.util.GeoPoint


class MyPointRepository(app: Application) {

    private val dao = MyPointDB.getInstance(app).myPointDao


    fun getAllMyPoints() = dao.getAllMyPoints()


    fun getMyPoint(id: Long) = dao.getMyPointById(id)

    fun getTrek(id: Long) = dao.getTrekById(id)


    suspend fun deleteMyPoint(point: MyPoint, trekLocations: TrekLocations?): Boolean {
        return try {
            trekLocations?.let {
                dao.deleteTrekLocations(it)
            }
            dao.deleteMyPoint(point)
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

    suspend fun insertMyPoint(
        myPoint: MyPoint,
        geoList: MutableList<MutableList<GeoPoint>>?
    ): Boolean {
        val pointId = dao.insertMyPoint(myPoint)
        geoList?.let {
            val trek = TrekLocations(pointId, it)
            dao.insertTrek(trek)
        }
        return true
    }
}
