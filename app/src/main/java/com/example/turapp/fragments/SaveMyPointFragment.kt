package com.example.turapp.fragments

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.databinding.FragmentSaveMyPointBinding
import com.example.turapp.utils.helperFiles.NAVIGATION_ARGUMENT_SAVING_TYPE
import com.example.turapp.viewmodels.SaveMyPointViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.*

class SaveMyPointFragment : Fragment() {

    private lateinit var binding: FragmentSaveMyPointBinding

    private lateinit var viewModel: SaveMyPointViewModel

    private var location: GeoPoint? = null
    private var image: String? = null
    private lateinit var map: MapView

    private var marker: Marker? = null

    private var disableScrollView : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val typeArgument = it.getString(NAVIGATION_ARGUMENT_SAVING_TYPE)
            location = it.get("location") as GeoPoint?
            image = it.getString("uri")
            if (typeArgument != null) {
                val app = requireNotNull(activity).application
                viewModel = ViewModelProvider(this, SaveMyPointViewModel.Factory(app, typeArgument))[SaveMyPointViewModel::class.java]
            } else {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                findNavController().navigate(SaveMyPointFragmentDirections.actionSaveMyPointFragmentToTrackingFragment())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveMyPointBinding.inflate(inflater)
        lifecycleScope.launchWhenCreated {
            map = binding.mapHolder
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
            map.apply {
                setMultiTouchControls(true) //3
                controller.setZoom(18.0)
            }
        }

        binding.btnSaveMyPoint.setOnClickListener {
            val title = binding.titleInputField.text.toString()
            val desc = binding.descInputField.text.toString()
            viewModel.saveSinglePoint(title = title, description = desc, marker = marker)
        }

        viewModel.finishedSavingPoint.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it)
                findNavController().navigate(SaveMyPointFragmentDirections.actionSaveMyPointFragmentToTrackingFragment())
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (location != null) {
            marker = Marker(map)
            marker?.apply {
                isDraggable = true
                title = getLocationInformation(location!!)
                showInfoWindow()
                setOnMarkerDragListener(getMarkerDragListener())
                position = location
            }
            map.overlays.add(marker)
            map.controller.animateTo(location)
            binding.btnGrpLocation.isChecked = true
        } else {
            binding.btnGrpLocation.isCheckable = false
            binding.btnGrpImage.isChecked = true
            binding.mapHolder.visibility = View.GONE
        }

        binding.btnGrpImage.addOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                binding.mapHolder.visibility = View.GONE

            } else {
                binding.mapHolder.visibility = View.VISIBLE
            }
        }
        binding.btnCancel.setOnClickListener {
            findNavController().navigate(SaveMyPointFragmentDirections.actionSaveMyPointFragmentToTrackingFragment())
        }
    }


    private fun getLocationInformation(p: GeoPoint): String? {
        val gc = Geocoder(requireContext(), Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(p.latitude, p.longitude, 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
    private fun getMarkerDragListener(): Marker.OnMarkerDragListener {
        return object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker?) {
                binding.scrollView.setScrollingEnabled(false)
            }

            override fun onMarkerDragEnd(marker: Marker?) {
                if (marker != null) {
                    marker.position = marker.position
                    marker.title = getLocationInformation(marker.position)
                    marker.showInfoWindow()
                    map.controller.animateTo(marker.position)
                    binding.scrollView.setScrollingEnabled(true)

                }
            }

            override fun onMarkerDragStart(marker: Marker?) {

            }

        }
    }
}