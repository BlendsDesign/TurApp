package com.example.turapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.MainActivity
import com.example.turapp.R
import com.example.turapp.databinding.FragmentArBinding
import com.example.turapp.utils.ArCoreUtils
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.utils.helperFiles.REQUEST_CODE_CAMERA_AND_LOCATION
import com.example.turapp.viewmodels.ArViewModel
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import org.osmdroid.util.GeoPoint
import pub.devrel.easypermissions.EasyPermissions
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.rendering.LocationNode
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

private const val TAG = "ARCoreCamera"

class ArFragment : Fragment() {

    private lateinit var binding : FragmentArBinding
    private var hasFinishedLoading = false
    private var installRequested = false

    private var arSceneView: ArSceneView? = null

    private var exampleLayoutRenderAble1: ViewRenderable? = null
    private var locationScene: LocationScene? = null
    private var layoutLocationMarker1: LocationMarker? = null

    private var loadingView: TextView? = null

    private val viewModel: ArViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, ArViewModel.Factory(app))[ArViewModel::class.java]
    }

    private var point: GeoPoint? = null
    private var latitude : Double = 0.0
    private var longitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            point = it.get("poi") as GeoPoint?
            latitude = point?.latitude!!
            longitude = point?.longitude!!
        }
    }

    private fun requestPermissions() {
        if (PermissionCheckUtility.hasCameraPermissions(requireContext()) and
            PermissionCheckUtility.hasLocationPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept camera permissions to use this app.",
            //REQUEST_CODE_CAMERA_PERMISSION,
            REQUEST_CODE_CAMERA_AND_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requestPermissions()

        binding = FragmentArBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel


        binding.fabClose.setOnClickListener {
            findNavController().popBackStack()
        }

        loadingView = binding.tvLoading

        arSceneView = binding.arSceneView


        val exampleLayout1: CompletableFuture<ViewRenderable> = ViewRenderable.builder()
            .setView(requireContext(), R.layout.ar_poi_marker)
            .build()

        CompletableFuture.allOf(exampleLayout1 /*, exampleLayout2*/)
            .handle<Any?> { notUsed: Void?, throwable: Throwable? ->
                if (throwable != null) {
                    ArCoreUtils.displayError(requireContext(), "Unable to load renderables", throwable)
                    return@handle null
                }
                try {
                    exampleLayoutRenderAble1 = exampleLayout1.get()
                    hasFinishedLoading = true
                } catch (ex: InterruptedException) {
                    ArCoreUtils.displayError(requireContext(), "Unable to load renderables", ex)
                } catch (ex: ExecutionException) {
                    ArCoreUtils.displayError(requireContext(), "Unable to load renderables", ex)
                }
                null
            }

        Log.d(TAG, "UpdateListener")

        arSceneView!!.scene.addOnUpdateListener { frameTime: FrameTime? ->
            if (!hasFinishedLoading) {
                return@addOnUpdateListener
            }
            if (locationScene == null) {
                locationScene = LocationScene(requireContext(), requireActivity(), arSceneView)
                layoutLocationMarker1 = LocationMarker(
                    longitude, latitude,
                    getPOIView(exampleLayoutRenderAble1)
                )

                // "onRender" event that renders every frame
                // updates the layout with the marker's distance
                layoutLocationMarker1!!.renderEvent = LocationNodeRender { node: LocationNode ->
                    val eView = exampleLayoutRenderAble1!!.view
                    val distanceTextView = eView.findViewById<TextView>(R.id.ar_distance)
                    distanceTextView.text = node.distance.toString().plus("m")
                }

                //adding the marker
                locationScene!!.mLocationMarkers.add(layoutLocationMarker1)
            }

            val frame = arSceneView!!.arFrame ?: return@addOnUpdateListener
            if (frame.camera.trackingState != TrackingState.TRACKING) {
                return@addOnUpdateListener
            }
            locationScene?.processFrame(frame)
            if (loadingView!!.visibility == View.VISIBLE) {
                for (plane in frame.getUpdatedTrackables(
                    Plane::class.java
                )) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        hideLoadingMessage()
                    }
                }
            }
        }

        Log.d("ARCoreCamera", "Request permission")

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getPOIView(renderable: ViewRenderable?): Node {
        Log.d("ARCoreCamera", "getExampleView")
        val base = Node()
        base.renderable = renderable
        val c: Context = requireContext()
        val eView = renderable?.view
        eView?.setOnTouchListener { v: View?, event: MotionEvent? ->
            Toast.makeText(c, "Location marker touched", Toast.LENGTH_LONG).show()
            false
        }
        return base
    }

    override fun onResume() {
        super.onResume()
        if (locationScene != null) {
            Log.d("ARCoreCamera", "resume locationscene")
            locationScene!!.resume()
        }
        if (arSceneView!!.session== null) {
            //if the session wasn't created yet, don't resume rendering.
            //This can happen if ARCore needs to be updated or permissions are not granted
            try {
                Log.d("ARCoreCamera", "DemoUtils create session")
                val session = ArCoreUtils.createArSession(requireActivity()/*,installRequested*/)
                if (session == null) {
                    Log.d("ARCoreCamera", "session == null")
                    //installRequested = ARLocationPermissionHelper.hasPermission(requireActivity())
                   return
                } else {
                    Log.d("ARCoreCamera", "setupSession")
                    arSceneView?.setupSession(session)
                    Log.d("ARCoreCamera", "setupSession done")
                }
            } catch (e: UnavailableException) {
                Log.d("ARCoreCamera", "exception in DemoUtils")
                ArCoreUtils.handleSessionException(requireActivity(), e)
            }
            try {
                Log.d("ARCoreCamera", "resume arSceneView")
                arSceneView!!.resume()
            } catch (ex: CameraNotAvailableException) {
                Log.d("ARCoreCamera", "exception in DemoUtils")
                ArCoreUtils.displayError(requireContext(), "Unable to get camera", ex)
                findNavController().popBackStack()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (arSceneView!!.session != null) {
                showLoadingMessage()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        if (locationScene != null) {
            Log.d(TAG, "onPause == null")
            locationScene!!.pause()
        }
        arSceneView!!.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        arSceneView!!.destroy()
    }

    private fun showLoadingMessage() {
        loadingView?.apply {
            //text = "Plane is loading"
            visibility = View.VISIBLE
        }
    }

    private fun hideLoadingMessage() {
        loadingView?.visibility = View.GONE
    }

}