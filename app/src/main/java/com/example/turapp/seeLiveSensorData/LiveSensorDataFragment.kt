package com.example.turapp.seeLiveSensorData

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentLiveSensorDataBinding

class LiveSensorDataFragment : Fragment() {

    private val viewModel: LiveSensorDataViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, LiveSensorDataViewModel.Factory(app)).get(LiveSensorDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLiveSensorDataBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.accSensorData.observe(viewLifecycleOwner, Observer {
            binding.tvAccSensor.text = it.toString()
        })

        viewModel.gyroSensorData.observe(viewLifecycleOwner, Observer {
            binding.tvGyroSensor.text = it.toString()
        })

        viewModel.dbFake.observe(viewLifecycleOwner, Observer {
            binding.tvOtherSensors.text = it
        })

        viewModel.magnetoSensorData.observe(viewLifecycleOwner, Observer {
            //binding.tvMagnetoSensor.text = it.toString()
        })

        viewModel.orientation.observe(viewLifecycleOwner, Observer {
            binding.tvMagnetoSensor.text = it.toString()
        })



        return binding.root
    }

}