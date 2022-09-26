package com.example.turapp.cameraView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentCameraBinding


class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding

    private val viewModel: CameraViewModel by lazy {
        ViewModelProvider(this, CameraViewModel.Factory())[CameraViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(context, arguments.toString(), Toast.LENGTH_SHORT).show()
        binding = FragmentCameraBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Inflate the layout for this fragment
        return binding.root
    }

}