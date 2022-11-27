package com.example.turapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.ActivitySettingBinding



class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    private var initialLimit: Int? = null

    private val viewModel: SettingsViewModel by lazy {
        val sharedPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        initialLimit = sharedPrefs.getInt("limit", 1)
        ViewModelProvider(this, SettingsViewModel.Factory(sharedPrefs))[SettingsViewModel::class.java]
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.toString()

        binding.sbSeekBar.progress = initialLimit?: 1

        binding.sbSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.setNumberOfPoints(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        binding.btnApplyLimit.setOnClickListener {
            viewModel.saveLimit()
            finish()
        }


        binding.switchDarkmode.apply {
            isChecked = viewModel.isNightMode.value ?: isUsingNightModeResources()
            //switch.isChecked = isUsingNightModeResources()
            setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    viewModel.setIsNight()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    viewModel.setIsNight()
                }
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