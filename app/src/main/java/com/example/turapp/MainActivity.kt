package com.example.turapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

class MainActivity : AppCompatActivity(), MenuProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        when (menuItem.itemId) {
            R.id.miSettings -> {
                Toast.makeText(this, "You clicked on settings", Toast.LENGTH_SHORT).show()
            }
            R.id.miHelp -> {
                Toast.makeText(this, "You clicked on help", Toast.LENGTH_SHORT).show()
            }
            R.id.miInfo -> {
                Toast.makeText(this, "You clicked on info", Toast.LENGTH_SHORT).show()
            }
            R.id.miClose -> {
                this?.finish() //shuts down the app
            }

        }
        return true
    }
}