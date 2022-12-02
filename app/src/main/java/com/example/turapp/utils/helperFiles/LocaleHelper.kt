package com.example.turapp.utils.helperFiles

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.*


class LocaleHelper(private val context: Context) {

    fun setLocale(language: String) {
        val appLocale: LocaleListCompat =
            LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getLocale(): Locale = context.resources.configuration.locales[0]

}