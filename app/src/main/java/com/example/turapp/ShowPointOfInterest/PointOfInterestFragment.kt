package com.example.turapp.ShowPointOfInterest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.turapp.R
import com.example.turapp.databinding.FragmentPointOfInterestBinding



class PointOfInterestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    private lateinit var _binding: FragmentPointOfInterestBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString("test")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPointOfInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        binding.tvTest.text = param1
        return binding.root
    }
}