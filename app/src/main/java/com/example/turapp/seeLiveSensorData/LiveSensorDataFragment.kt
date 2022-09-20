package com.example.turapp.seeLiveSensorData

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.Sensors.AccelerometerSensor
import com.example.turapp.databinding.FragmentLiveSensorDataBinding

class LiveSensorDataFragment : Fragment() {

    private val viewModel: LiveSensorDataViewModel by lazy {
        val app = requireNotNull(activity).application
        val sensor = AccelerometerSensor(app)
        ViewModelProvider(this, LiveSensorDataViewModel.Factory(sensor)).get(LiveSensorDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLiveSensorDataBinding.inflate(inflater)
        binding.lifecycleOwner = this

        viewModel.sensorData.observe(viewLifecycleOwner, Observer {
            binding.tester.text = it.toString()
        })



        return binding.root
    }

}