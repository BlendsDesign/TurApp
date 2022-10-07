package com.example.turapp.mapView

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentMapBinding
import com.example.turapp.startPage.StartFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.turapp.mapView.MapViewModel


class MapFragment : Fragment() {

    private lateinit var binding : FragmentMapBinding

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this, MapViewModel.Factory())[MapViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner


        binding.viewModel = viewModel

        // Inflate the layout for this fragment
        return binding.root
    }
}