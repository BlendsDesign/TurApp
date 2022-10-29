package com.example.turapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentCameraBinding
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.viewmodels.CameraViewModel
import com.example.turapp.utils.TrackingCamera


class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding
    private val camPERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private lateinit var cameraView: TextureView
    private lateinit var cam : TrackingCamera

    private val viewModel: CameraViewModel by lazy {
        ViewModelProvider(this, CameraViewModel.Factory())[CameraViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //camera permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), camPERMISSIONS, 1)
            return
        }

        //or may be just...

       if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
           return
       }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        cameraView = binding.cameraView

        cam = TrackingCamera(requireContext(),cameraView);
        cameraView.surfaceTextureListener = cam.cameraListener

        // Inflate the layout for this fragment
        return binding.root
    }
}