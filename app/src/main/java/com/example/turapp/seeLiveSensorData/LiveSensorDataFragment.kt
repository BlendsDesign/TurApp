package com.example.turapp.seeLiveSensorData

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.R
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

        viewModel.tempAccSensorRec.observe(viewLifecycleOwner, Observer {
            binding.tvAccSensorRecording.text = it.toString()
        })
        viewModel.tempGyroSensorRec.observe(viewLifecycleOwner, Observer {
            binding.tvGyroSensorRecording.text = it.toString()
        })
        binding.btRecord.apply {
            setBackgroundColor(Color.BLUE)
            text = getString(R.string.record)
            setOnClickListener {
                viewModel.startRec()
            }
        }
        viewModel.recording.observe(viewLifecycleOwner, Observer {
            if(it) {
                binding.btRecord.apply {
                    setBackgroundColor(Color.RED)
                    text = getString(R.string.stop_recording)
                }
                binding.btRecord.setOnClickListener {
                    viewModel.stopRec()
                }
            } else {
                binding.btRecord.apply {
                    setBackgroundColor(Color.BLUE)
                    text = getString(R.string.record)
                }

                binding.btRecord.setOnClickListener {
                    viewModel.startRec()
                }
            }
        })

        return binding.root
    }

}