package com.example.turapp

import android.content.ContextWrapper
import android.os.Build
import org.osmdroid.config.Configuration
import java.io.File

object Helper {
    fun suggestedFix(contextWrapper: ContextWrapper) {
        val root = contextWrapper.filesDir
        val osmDroidBasePath = File(root, "osmdroid")
        osmDroidBasePath.mkdirs()
        Configuration.getInstance().osmdroidBasePath = osmDroidBasePath
    }
}