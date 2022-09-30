package com.example.turapp.ShowPointOfInterest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentPointOfInterestBinding


class PointOfInterestFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var poi: Int = -1

    private lateinit var _binding: FragmentPointOfInterestBinding
    private val binding get() = _binding

    private lateinit var viewModel: PointOfInterestViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = requireNotNull(activity).application
        arguments?.let {
            poi = it.getInt("poiId")
        }
        viewModel = ViewModelProvider(this, PointOfInterestViewModel.Factory(app, poi))
            .get(PointOfInterestViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPointOfInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        viewModel.poi.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.tvTest.text = it[0].recording[0].recording.get(5).toString()
            }
        })

        return binding.root
    }
}