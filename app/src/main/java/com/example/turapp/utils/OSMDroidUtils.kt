package com.example.turapp.utils

import android.content.Context
import android.util.Log
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider
import org.osmdroid.tileprovider.modules.TileWriter
import org.osmdroid.util.GeoPoint

object OSMDroidUtils {
    fun cacheTilesFromListOfGeoPoint(list: ArrayList<GeoPoint>) {
        //CacheManager.TODO Use cache manager to download tiles related to the geodata we have stored
        CacheManager.getTilesCoverage(list, 10, 29)
        Log.d("OSMDROIDUTILS", "ran cacheTilesFromListOfGeoPoint")
    }
}
