package com.example.turapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.turapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.turToolbar))
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

    }
}