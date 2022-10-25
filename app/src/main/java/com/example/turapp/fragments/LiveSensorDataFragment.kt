package com.example.turapp.fragments

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
import com.example.turapp.viewmodels.LiveSensorDataViewModel

class LiveSensorDataFragment : Fragment() {

    private val viewModel: LiveSensorDataViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(
            this,
            LiveSensorDataViewModel.Factory(app)
        )[LiveSensorDataViewModel::class.java]

    }

    private var _binding: FragmentLiveSensorDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set up DataBinding
        _binding = FragmentLiveSensorDataBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        // Set up initial bindings
        binding.apply {
            btRecord.apply {
                setBackgroundColor(Color.BLUE)
                text = getString(R.string.record)
                setOnClickListener {
                    viewModel?.startRec()
                }
            }
            switchAccRec.setOnClickListener {
                viewModel?.setRecAccSensorData()
            }
            switchGyroRec.setOnClickListener {
                viewModel?.setRecGyroSensorData()
            }
            switchMagnetoRec.setOnClickListener {
                viewModel?.setRecMagnetoSensorData()
            }
            switchOrientationRec.setOnClickListener {
                viewModel?.setRecOrientationSensorData()
            }
        }
        // Setting up observers to give LiveData to the textViews
        viewModel.accSensorData.observe(viewLifecycleOwner, Observer {
            binding.tvAccSensor.text = String.format("X: %.2f Y: %.2f Z: %.2f", it[0], it[1], it[2])
        })

        viewModel.accSensorDataFiltered.observe(viewLifecycleOwner, Observer {
            binding.tvFilteredAccData.text = String.format("X: %.2f Y: %.2f Z: %.2f", it[0], it[1], it[2])
        })

        viewModel.gyroSensorData.observe(viewLifecycleOwner, Observer {
            binding.tvGyroSensor.text = String.format("X: %.2f Y: %.2f Z: %.2f", it[0], it[1], it[2])
        })

        viewModel.gyroSensorDataFiltered.observe(viewLifecycleOwner, Observer {
            binding.tvFilteredGyroData.text = String.format("X: %.2f Y: %.2f Z: %.2f", it[0], it[1], it[2])
        })

        viewModel.magnetoSensorData.observe(viewLifecycleOwner, Observer {
            binding.tvMagnetoSensor.text = String.format("X: %.2f Y: %.2f Z: %.2f", it[0], it[1], it[2])
        })

        viewModel.orientationData.observe(viewLifecycleOwner, Observer {
            binding.tvOrientationData.text = String.format("X: %.2f Y: %.2f Z: %.2f", it[0], it[1], it[2])
        })

        // Setting up Observer to know if we are recording or not
        viewModel.recording.observe(viewLifecycleOwner, Observer {
            if (it) {
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

    override fun onPause() {
        super.onPause()
        if (viewModel.recording.value == true) {
            viewModel.stopRec()
        }
    }

}