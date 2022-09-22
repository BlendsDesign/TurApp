package com.example.turapp.mapView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentMapBinding


class MapFragment : Fragment() {

    private lateinit var binding : FragmentMapBinding

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this, MapViewModel.Factory())[MapViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(context, arguments.toString(), Toast.LENGTH_SHORT).show()
        binding = FragmentMapBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        // Inflate the layout for this fragment
        return binding.root
    }

}