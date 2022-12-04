package com.example.turapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentSelfieBinding
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.SelfieCamera
import com.example.turapp.utils.helperFiles.NAVIGATION_ARGUMENT_SAVING_TYPE
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.viewmodels.SelfieViewModel
import com.example.turapp.viewmodels.TrackingViewModel
import org.osmdroid.util.GeoPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SelfieFragment : Fragment() {

    private lateinit var binding: FragmentSelfieBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraView: PreviewView
    private lateinit var selfieCam: SelfieCamera
    private var pictureLocation: GeoPoint? = null

    private lateinit var viewModel: SelfieViewModel

    private var imageRotation = Surface.ROTATION_0

    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientaion: Int) {
                if (orientaion == ORIENTATION_UNKNOWN) {
                    return
                }

                val rotation = when(orientaion) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageRotation = rotation
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            var typeArgument = it.getString(NAVIGATION_ARGUMENT_SAVING_TYPE)
            if (typeArgument == null) {
                typeArgument = TYPE_SNAPSHOT
            }
            val app = requireNotNull(activity).application
            viewModel = ViewModelProvider(
                this, SelfieViewModel.Factory(app, typeArgument)
            )[SelfieViewModel::class.java]
        }
        if (!PermissionCheckUtility.hasCameraPermissions(requireContext())) {
            Toast.makeText(requireContext(), getString(R.string.missing_camera_permissions), Toast.LENGTH_SHORT)
                .show()
            if (viewModel.typeArgument == TYPE_TRACKING) {
                findNavController().navigate(
                    SelfieFragmentDirections.actionSelfieFragmentToSaveMyPointFragment(
                        viewModel.typeArgument, null, null
                    )
                )
            } else {
                findNavController().popBackStack()
            }
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

        viewModel.selectedCamera.observe(viewLifecycleOwner) {
            it.let {
                selfieCam = SelfieCamera(requireContext(), cameraView, this, it)
                selfieCam.startCamera()
                binding.selfieCaptureButton.setOnClickListener {
                    selfieCam.takePhoto(getImageSavedCallback(), imageRotation)
                }
            }
        }

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


        binding.btnCancelSelfie.setOnClickListener {
            if (viewModel.pictureUri.value != null) {
                viewModel.deleteTakenPicture()
            } else {
                when (viewModel.typeArgument) {
                    TYPE_SNAPSHOT -> {
                        findNavController().navigate(SelfieFragmentDirections.actionSelfieFragmentToTrackingFragment())
                    }
                    else -> {
                        findNavController().navigate(
                            SelfieFragmentDirections.actionSelfieFragmentToSaveMyPointFragment(
                                viewModel.typeArgument, null, null
                            )
                        )
                    }
                }
            }
        }
        binding.btnSaveImage.setOnClickListener {
            viewModel.savePicture()

            findNavController().navigate(
                SelfieFragmentDirections.actionSelfieFragmentToSaveMyPointFragment(
                    viewModel.typeArgument,
                    pictureLocation,
                    viewModel.pictureUri.value
                )
            )
        }

        // Observe if we have a picture
        viewModel.pictureUri.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.btnSwichCamera.visibility = View.GONE
                binding.btnSaveImage.visibility = View.VISIBLE
                binding.selfieCaptureButton.visibility = View.GONE
            } else {
                binding.btnSwichCamera.visibility = View.VISIBLE
                binding.btnSaveImage.visibility = View.GONE
                binding.selfieCaptureButton.visibility = View.VISIBLE
            }
        }

        // Return the Layout
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    private fun getImageSavedCallback(): ImageCapture.OnImageSavedCallback {
        return object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("SelfieCamera", "Photo capture failed: ${exception.message}", exception)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                val test = output.savedUri
                if (test != null) {
                    viewModel.setPictureUri(test)
                    TrackingViewModel.getLocation.value?.let {
                        pictureLocation = GeoPoint(it)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.keepPicture.value == true)
            viewModel.resetKeepPicture()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        if (viewModel.keepPicture.value != true) {
            viewModel.deleteTakenPicture()
        }
    }
}