package com.example.turapp.startPage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.R
import com.example.turapp.databinding.StartFragmentBinding
import kotlinx.android.synthetic.main.activity_main.*

class StartFragment : Fragment(), MenuProvider {

    private val viewModel: StartViewModel by lazy {
        val test = "velkommen til appen"
        ViewModelProvider(this, StartViewModel.Factory(test)).get(StartViewModel::class.java)
    }

    private lateinit var binding: StartFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding = StartFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        //binding.btnNextFrag.setOnClickListener { findNavController().navigate(StartFragmentDirections
        //    .actionStartFragmentToLiveSensorDataFragment()) }
        binding.navShowTides.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.btnGoToSensor -> findNavController().navigate(StartFragmentDirections.actionStartFragmentToLiveSensorDataFragment())
                R.id.miCamera -> findNavController().navigate(StartFragmentDirections.actionStartFragmentToCameraScreenFragment())
                R.id.miMap -> findNavController().navigate(StartFragmentDirections.actionStartFragmentToMapScreenFragment())

            }
            true
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //List of locations with distance from user (RecyclerView)
        var locationsList = mutableListOf(
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

        binding.rvLocations.apply {
            adapter = LocationAdapter(locationsList)
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        when (menuItem.itemId) {
            R.id.miSettings -> {
                Toast.makeText(activity, "You clicked on settings", Toast.LENGTH_SHORT).show()
            }
            R.id.miHelp -> {
                Toast.makeText(activity, "You clicked on help", Toast.LENGTH_SHORT).show()
            }
            R.id.miInfo -> {
                Toast.makeText(activity, "You clicked on info", Toast.LENGTH_SHORT).show()
            }
            R.id.miClose -> {
                activity?.finish() //shuts down the app
            }

        }
        return true
    }



}