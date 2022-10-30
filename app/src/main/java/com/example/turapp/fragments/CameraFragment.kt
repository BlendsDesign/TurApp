package com.example.turapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentCameraBinding
import com.example.turapp.viewmodels.CameraViewModel
import com.example.turapp.utils.ShotCamera
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView
import com.example.turapp.utils.helperFiles.PermissionCheckUtility

class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding

    private val camPERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraView: PreviewView
    private lateinit var shotCam : ShotCamera

    private val viewModel: CameraViewModel by lazy {
        ViewModelProvider(this, CameraViewModel.Factory())[CameraViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        //camera permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), camPERMISSIONS, 1)
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

        shotCam = ShotCamera(requireContext(),cameraView, this);

        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener { shotCam.takePhoto() }

        if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            shotCam.startCamera()
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}