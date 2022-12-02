package com.example.turapp.utils.helperFiles

import android.content.ContextWrapper
import android.os.Build
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import org.osmdroid.config.Configuration
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Helper {
    fun suggestedFix(contextWrapper: ContextWrapper) {
        val root = contextWrapper.filesDir
        val osmDroidBasePath = File(root, "osmdroid")
        osmDroidBasePath.mkdirs()
        Configuration.getInstance().osmdroidBasePath = osmDroidBasePath
    }
}

class GraphValueFormatter(private val pointList: List<MyPoint>): ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (value.toInt().toFloat() != value) {
            return ""
        }
        return pointList.getOrNull(value.toInt())
            ?.createdAt?.let { date ->
                DateTimeFormatter.ofPattern("dd/MMM").format(
                    ZonedDateTime.ofInstant(Date(date).toInstant(), ZoneId.systemDefault()))
            } ?: ""
    }
}
