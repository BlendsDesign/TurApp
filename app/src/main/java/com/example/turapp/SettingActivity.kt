package com.example.turapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.ActivitySettingBinding
import com.example.turapp.utils.helperFiles.LocaleHelper
import kotlinx.android.synthetic.main.activity_setting.*
import java.util.*


class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    private lateinit var localeHelper: LocaleHelper

    private val viewModel: SettingsViewModel by lazy {
        val sharedPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        ViewModelProvider(this, SettingsViewModel.Factory(sharedPrefs))[SettingsViewModel::class.java]
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localeHelper = LocaleHelper(this)

        viewModel.limit.value?.let {
            binding.sbSeekBar.progress = it
        }

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

        setSpinner(binding.spLanguages,listOf("English","Norwegian"))

        btnApplyLanguage.setOnClickListener {
            val language = binding.spLanguages.selectedItem.toString()
            if (language == "English"){
                setAppLocale("en")
            }else{
                setAppLocale("no")
            }
        }

        binding.btnCancel.setOnClickListener { finish() }


    }

    fun setSpinner(spinner: Spinner, spinnerList : List<String>) {
        val adapter = object :
            ArrayAdapter<Any>(
                this,android.R.layout.simple_list_item_1,android.R.id.text1,
                spinnerList
            ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return super.getDropDownView(position, convertView, parent).also {
                    if (position == spinner.selectedItemPosition) {
                        it.setBackgroundColor(ContextCompat.getColor(this@SettingActivity,R.color.purple_200))
                    }
                }
            }
        }
        spinner.adapter = adapter
        spinner.setSelection(if (localeHelper.getLocale().language == "no") 1 else 0)
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

    fun setAppLocale(language : String) {
        Log.d("SettingsActivity", "setAppLocale: " + language)
        localeHelper.setLocale(language)
    }

}