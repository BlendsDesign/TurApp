package com.example.turapp.ShowPointOfInterest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.turapp.R
import com.example.turapp.databinding.FragmentPointOfInterestBinding
import com.example.turapp.startPage.Location


class PointOfInterestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var location: Location? = null

    private lateinit var _binding: FragmentPointOfInterestBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            location = it.getParcelable("point", Location::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPointOfInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        binding.tvTest.text = location.toString()
        return binding.root
    }
}