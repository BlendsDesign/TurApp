package com.example.turapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.turapp.startPage.StartFragmentDirections
import com.example.turapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.turToolbar))
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(R.layout.activity_main)


    }
//    binding.navShowTides.setOnItemSelectedListener {
//        when(it.itemId) {
//            R.id.btnGoToSensor -> findNavController().navigate(StartFragmentDirections.actionStartFragmentToLiveSensorDataFragment())
//            R.id.miCamera -> findNavController().navigate(StartFragmentDirections.actionStartFragmentToCameraScreenFragment())
//            R.id.miMap -> findNavController().navigate(StartFragmentDirections.actionStartFragmentToMapScreenFragment())
//
//        }
//        true
//    }
}