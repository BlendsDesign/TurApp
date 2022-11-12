package com.example.turapp.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentSelfieBinding
import com.example.turapp.utils.SelfieCamera
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.utils.helperFiles.REQUEST_CODE_CAMERA_PERMISSION
import com.example.turapp.viewmodels.SelfieViewModel
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SelfieFragment : Fragment() {

    private lateinit var binding : FragmentSelfieBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraView: PreviewView
    private lateinit var selfieCam: SelfieCamera

    private val viewModel: SelfieViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, SelfieViewModel.Factory(app))[SelfieViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        requestPermissions()

        // TODO ASK FOR PERMISSIONS
        if (!PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            Toast.makeText(requireContext(), "Missing Camera Permissions", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.trackingFragment, false)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSelfieBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        cameraView = binding.selfieCameraView

        if (!PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            findNavController().popBackStack()
        }

        selfieCam = SelfieCamera(requireContext(), cameraView, this)

        viewModel.selectedCamera.observe(viewLifecycleOwner, Observer {
            it.let {
                selfieCam = SelfieCamera(requireContext(), cameraView, this, it)
                selfieCam.startCamera()
                binding.selfieCaptureButton.setOnClickListener {
                    selfieCam.takePhoto(getImageSavedCallback())
                }
            }
        })

        binding.btnSwichCamera.apply {
            addOnCheckedChangeListener { button, isChecked ->
                when(isChecked) {
                    true -> {
                        button.isChecked = false
                    }
                    else -> {}
                }
            }
            setOnClickListener {
                viewModel.setSelectedCamera()
            }
        }


        binding.btnCancelSelfie.addOnCheckedChangeListener { button, isChecked ->
            when(isChecked) {
                true -> {
                    button.isChecked = false
                }
                else -> {}
            }
        }
        binding.btnCancelSelfie.setOnClickListener {
            if (viewModel.pictureUri.value != null) {
                viewModel.deleteTakenPicture()
            } else {
                // TODO This is a simplification, we want to go to SaveMyPointFragment to SAVE THE RUN
                findNavController().navigate(SelfieFragmentDirections.actionSelfieFragmentToTrackingFragment())
            }
        }

        // Observe if we have a picture
        // TODO Implement save picture button
        viewModel.pictureUri.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                binding.btnSwichCamera.visibility = View.GONE
                binding.btnSaveImage.visibility = View.VISIBLE
            } else {
                binding.btnSwichCamera.visibility = View.VISIBLE
                binding.btnSaveImage.visibility = View.GONE
            }
        })



        return binding.root
    }

    private fun getImageSavedCallback(): ImageCapture.OnImageSavedCallback {
        return object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("SelfieCamera", "Photo capture failed: ${exception.message}", exception)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${output.savedUri}"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Log.d("ShotCamera", msg)
                //viewModel.setTakingPicture()
                val test = output.savedUri
                if(test != null) {
                    viewModel.setPictureUri(test)
                }
            }
        }
    }

    companion object {}

    private fun requestPermissions() {
        if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept camera permissions to use this app.",
            REQUEST_CODE_CAMERA_PERMISSION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        if(viewModel.keepPicture.value != true) {
            viewModel.deleteTakenPicture()
        }
    }
}