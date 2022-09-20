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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.startPage.Location
import com.example.turapp.startPage.LocationAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MenuProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.turToolbar))
        setContentView(R.layout.activity_main)

        //List of locations with distance from user (RecyclerView)
        val locationsList = mutableListOf(
            Location("Canary River", 111 ),
            Location("Sweet Canyon", 222 ),
            Location("Country Road", 333 ),
            Location("Cotton Fields", 444 ),
            Location("Death Valley", 555 ),
            Location("Scary Forest", 666 ),
            Location("Twin Peaks", 777 ),
            Location("Fishing Spot", 888 ),
            Location("Hunting ground", 999 ),
            Location("Steep Hill", 132 ),

        )

        val adapter = LocationAdapter(locationsList)
        rvLocations.adapter = adapter
        rvLocations.layoutManager = LinearLayoutManager(this)

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