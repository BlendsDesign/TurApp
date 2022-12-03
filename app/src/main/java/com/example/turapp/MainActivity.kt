package com.example.turapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.turapp.databinding.ActivityMainBinding
import com.example.turapp.fragments.TrackingFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPrefs()
        navController = findViewById<View?>(R.id.navHostFragment).findNavController()
        binding.bottomNav.setupWithNavController(navController)


        navController.apply {
            addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.graphFragment,
                    R.id.trackingFragment,
                    R.id.listFragment,
                    R.id.selfieFragment -> {
                        binding.bottomNav.visibility = View.VISIBLE
                    }
                    else -> binding.bottomNav.visibility = View.GONE

                }
            }
        }
        setSupportActionBar(binding.turToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.miSettings -> {
                startActivity(Intent(this, SettingActivity::class.java))
                //return true
            }
            R.id.miInfo -> {
                val linearLayout = LinearLayout(this)
                linearLayout.orientation = LinearLayout.VERTICAL;
                val image = ImageView(this@MainActivity)
                linearLayout.addView(image)
                image.setImageResource(R.drawable.infoimage2)
                //val alertDialog = AlertDialog.Builder(this@MainActivity, R.layout.dialog_info).create()
                val alertDialog = AlertDialog.Builder(this@MainActivity, R.style.MyInfoTheme).create()
                    //android.R.style.Theme_Translucent_NoTitleBar_Fullscreen).create()
                alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL, "OK"
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }

                alertDialog.setView(linearLayout)
                alertDialog.show()

            }
            else -> {
                val btnQuit = findViewById<Button>(R.id.miClose)
                btnQuit.setOnClickListener {
                    doQuit()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doQuit() {
        Toast.makeText(this, "Quitting!", Toast.LENGTH_SHORT).show()

    }

    private fun checkPrefs() {
        val sharedPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        if (sharedPrefs.contains("isNight") && sharedPrefs.getBoolean("isNight", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

}