package com.example.turapp.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentCameraBinding
import com.example.turapp.viewmodels.CameraViewModel
import com.example.turapp.utils.ShotCamera
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.utils.helperFiles.REQUEST_CODE_CAMERA_PERMISSION
import pub.devrel.easypermissions.EasyPermissions

class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraView: PreviewView
    private lateinit var shotCam : ShotCamera
    private var pathToPicture : String? = null

    private val viewModel: CameraViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, CameraViewModel.Factory(app))[CameraViewModel::class.java]
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

        if (PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            shotCam.startCamera()
        }


        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener {
            shotCam.takePhoto(getImageSavedCallback())
            //pathToPicture = shotCam.getPath()
            //setFragmentResult("requestKey", bundleOf("bundleKey" to pathToPicture))
        }

//        viewModel.takingPicture.observe(viewLifecycleOwner, Observer {
//            if (it == true) {
//                viewModel.setTakingPicture() //reset when returning to this fragment
//                findNavController().navigate(
//                    CameraFragmentDirections.actionCameraFragmentToSavePictureFragment()
//                )
//            }
//        })
        //https://gaumala.com/posts/2020-05-03-navigating-with-fragments.html
//        val pictureFragment = SavePictureFragment()
//        parentFragmentManager.beginTransaction()
//            .replace(R.id.savePictureFragment,pictureFragment)



        // Inflate the layout for this fragment
        return binding.root
    }

    private fun getImageSavedCallback(): ImageCapture.OnImageSavedCallback {
        return object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("ShotCamera", "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults){
                val msg = "Photo capture succeeded: ${output.savedUri}"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Log.d("ShotCamera", msg)
                //viewModel.setTakingPicture()
                val test : String? = output.savedUri?.path
                if(test != null) {
                    findNavController().navigate(CameraFragmentDirections
                        .actionCameraFragmentToSavePictureFragment(test))
                    //viewModel.saveMyPoint(test)

                }

            }
        }
    }

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
    }
}