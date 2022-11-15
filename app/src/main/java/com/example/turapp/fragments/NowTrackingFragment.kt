package com.example.turapp.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentNowTrackingBinding
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.helperFiles.PermissionCheckUtility
import com.example.turapp.viewmodels.NowTrackingViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

class NowTrackingFragment : Fragment() {

    private lateinit var binding: FragmentNowTrackingBinding

    private lateinit var orientationProvider: InternalCompassOrientationProvider

    private lateinit var map: MapView

    private val clMark : Marker by lazy {
        Marker(map).apply {
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location_arrow)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }

    private val viewModel: NowTrackingViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, NowTrackingViewModel.Factory(app))[NowTrackingViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        if(!PermissionCheckUtility.hasLocationPermissions(requireContext())) {
            Toast.makeText(requireContext(), "Missing Location Permission", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }
        orientationProvider = InternalCompassOrientationProvider(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNowTrackingBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launchWhenCreated {
            map = binding.trackingMap
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.overlays.add(clMark)
            map.controller.setZoom(18.0)
            viewModel.currentLocation.observe(viewLifecycleOwner, Observer {
                clMark.position = GeoPoint(it)
                map.controller.animateTo(clMark.position)
            })
            viewModel.tracked.observe(viewLifecycleOwner, Observer { outerList ->
                if (outerList.isNotEmpty()) {
                    outerList.forEach { innerList ->
                        val poli = Polyline()
                        poli.color = Color.RED
                        poli.setPoints(innerList)
                        map.overlays.add(poli)
                        map.invalidate()
                    }
                }
            })
        }

        viewModel.timer.observe(viewLifecycleOwner, Observer {
            binding.tvShowTimer.text = getFormattedTimerString(it)
        })

        viewModel.steps.observe(viewLifecycleOwner, Observer {
            binding.tvShowSteps.text = "Steps: $it"
        })

        binding.btnPause.addOnCheckedChangeListener { button, isChecked ->
            when(isChecked) {
                true -> {
                    viewModel.pauseService()
                    button.icon = resources.getDrawable(R.drawable.ic_play_arrow)
                    button.text = "Resume"
                }
                false -> {
                    viewModel.resumeService()
                    button.icon = resources.getDrawable(R.drawable.ic_pause)
                    button.text = "Pause"
                }
            }
        }
        binding.btnStop.addOnCheckedChangeListener { button, isChecked ->
            when(isChecked) {
                true -> {
                    viewModel.saveTreck()
                    findNavController().navigate(
                        NowTrackingFragmentDirections.actionNowTrackingFragmentToSelfieFragment(TYPE_TRACKING)
                    )
                    button.apply {
                        text = "Restart Service"
                        icon = resources.getDrawable(R.drawable.ic_play_arrow)
                    }
                }
                false -> {
                    viewModel.resumeService()
                    button.apply {
                        text = "Stop"
                        icon = resources.getDrawable(R.drawable.ic_stop)
                    }
                }
            }
        }

        viewModel.hasStoppedService.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                viewModel.resetFinishedSaving()
            }
        })

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.hasStoppedService.value != true)
            viewModel.cancelTreck()
    }

    private fun getFormattedTimerString(timeHundreds: Long) : String {
        val hundreds = timeHundreds % 100
        val seconds = (timeHundreds / 100) % 60
        val minutes = (timeHundreds / 60000) % 60
        val hours = (timeHundreds / 3600000)
        val hoursString = if (hours < 10) "0$hours:" else "$hours:"
        val minutesString = if (minutes < 10) "0$minutes:" else "$minutes:"
        val secondsString = if (seconds < 10) "0$seconds:" else "$seconds:"
        val hundredsString = if (hundreds < 10) "0$hundreds" else "$hundreds"
        return if (hours < 1)
            minutesString + secondsString + hundredsString
        else
            hoursString + minutesString + secondsString + hundredsString
    }

}