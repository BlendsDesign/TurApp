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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.turapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.turToolbar))
        setContentView(R.layout.activity_main)

        checkPrefs()

        binding = ActivityMainBinding.inflate(layoutInflater)

        val myToolbar = findViewById<View>(R.id.turToolbar) as Toolbar
        setSupportActionBar(myToolbar)

    }

    override fun onRestart() {
        super.onRestart()
        val test = getSharedPreferences("myPrefs", Context.MODE_PRIVATE).getBoolean("isNight", false).toString()
        Toast.makeText(this, test, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.miSettings -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
            R.id.miHelp -> {
                val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                alertDialog.setTitle("Help")
                alertDialog.setMessage("Info info info")
                alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL, "OK"
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                alertDialog.show()
            }
            R.id.miInfo -> {
                val toast = Toast.makeText(this, "Information...", Toast.LENGTH_LONG)
                toast.show()
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