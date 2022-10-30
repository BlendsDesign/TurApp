package com.example.turapp.fragments

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentCameraBinding
import com.example.turapp.viewmodels.CameraViewModel
import com.example.turapp.utils.ShotCamera
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.utils.helperFiles.REQUEST_CODE_CAMERA_PERMISSION
import pub.devrel.easypermissions.EasyPermissions

class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraView: PreviewView
    private lateinit var shotCam : ShotCamera

    private val viewModel: CameraViewModel by lazy {
        ViewModelProvider(this, CameraViewModel.Factory())[CameraViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requestPermissions()

        binding = FragmentCameraBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        cameraView = binding.cameraView

        shotCam = ShotCamera(requireContext(),cameraView, this);

        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener { shotCam.takePhoto() }

        if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            shotCam.startCamera()
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun requestPermissions() {
        if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app.",
            REQUEST_CODE_CAMERA_PERMISSION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}