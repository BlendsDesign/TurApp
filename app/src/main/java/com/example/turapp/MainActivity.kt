package com.example.turapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.startPage.*
import com.google.android.material.navigation.NavigationBarView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MenuProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.turToolbar))
        setContentView(R.layout.activity_main)

        // BASED ON https://youtu.be/HtwDXRWjMcU

        //List of locations with distance from user (RecyclerView)
//        var locationsList = mutableListOf(
//            Location("Canary River", 111 ),
//            Location("Sweet Canyon", 222 ),
//            Location("Country Road", 333 ),
//            Location("Cotton Fields", 444 ),
//            Location("Death Valley", 555 ),
//            Location("Scary Forest", 666 ),
//            Location("Twin Peaks", 777 ),
//            Location("Fishing Spot", 888 ),
//            Location("Hunting ground", 999 ),
//            Location("Steep Hill", 132 ),
//
//        )

//        val adapter = LocationAdapter(locationsList)
//        rvLocations.adapter = adapter
//        rvLocations.layoutManager = LinearLayoutManager(this)


        // A TEST BASED ON https://youtu.be/AL_1UDa9l3U
//        val startFragment = StartFragment()
//        val fragmentTest1 = FragmentTest1()
//        val fragmentTest2 = FragmentTest2()
//        val fragmentTest3 = FragmentTest3()
//
//        setCurrentFragment(fragmentTest1)
//
//        NavigationBarView.OnItemSelectedListener{
//            when(it.itemId)
//            {
//                R.id.miHome -> setCurrentFragment(startFragment)
//                R.id.miCamera -> setCurrentFragment(fragmentTest2)
//                R.id.miMap -> setCurrentFragment(fragmentTest3)
//            }
//            true
//        }
//    }
//
//    private fun setCurrentFragment(fragment: Fragment) = //switch between fragments
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.flFragment, fragment)
//            commit()
//        }
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