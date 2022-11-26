package com.example.turapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider


class SettingActivity : AppCompatActivity() {

    private var limitChangedValue = 0

    private val viewModel: SettingsViewModel by lazy {
        val sharedPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        ViewModelProvider(this, SettingsViewModel.Factory(sharedPrefs))[SettingsViewModel::class.java]
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val seekBar  = findViewById<SeekBar>(R.id.sbSeekBar)
        seekBar.min = 1 //0 means all points

        val pref = getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        seekBar.progress = pref.getInt("limit",5)


        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                limitChangedValue = progress
                viewModel.setNumberOfPoints(limitChangedValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(
                    this@SettingActivity, "Seek bar progress is :$limitChangedValue",
                    Toast.LENGTH_SHORT
                ).show()
                //savedInstanceState!!.putInt("currentLimit",limitChangedValue)
            }
        })


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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("currentLimit", limitChangedValue)
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