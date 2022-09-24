package com.example.turapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider

class SettingActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by lazy {
        val sharedPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        ViewModelProvider(this, SettingsViewModel.Factory(sharedPrefs))[SettingsViewModel::class.java]
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val switch = findViewById<Switch>(R.id.switch_darkmode)

        switch.isChecked = viewModel.isNightMode.value?: isUsingNightModeResources()
        //switch.isChecked = isUsingNightModeResources()

        switch.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                viewModel.setIsNight()
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                viewModel.setIsNight()
            }
        }


    }


    private fun isUsingNightModeResources(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

}