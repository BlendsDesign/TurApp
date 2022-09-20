package com.example.turapp.seeLiveSensorData

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.R
import com.example.turapp.databinding.FragmentLiveSensorDataBinding

class LiveSensorDataFragment : Fragment() {

    private val viewModel: LiveSensorDataViewModel by lazy {
        ViewModelProvider(this, LiveSensorDataViewModel.Factory()).get(LiveSensorDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLiveSensorDataBinding.inflate(inflater)
        binding.lifecycleOwner = this


        return binding.root
    }

}