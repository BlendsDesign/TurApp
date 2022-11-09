package com.example.turapp.fragments

import android.content.ContextWrapper
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentSaveMyPointBinding
import com.example.turapp.utils.helperFiles.Helper
import com.example.turapp.viewmodels.SaveMyPointViewModel
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.*

class SaveMyPointFragment : Fragment() {

    private lateinit var binding: FragmentSaveMyPointBinding

    private val viewModel: SaveMyPointViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, SaveMyPointViewModel.Factory(app))[SaveMyPointViewModel::class.java]
    }

    private var location: GeoPoint? = null
    private lateinit var map: MapView

    private var disableScrollView : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Helper.suggestedFix(contextWrapper = ContextWrapper(context))
        arguments?.let {
            location = it.get("location") as GeoPoint?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveMyPointBinding.inflate(inflater)

        map = binding.mapHolder
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
        map.apply {
            setMultiTouchControls(true) //3
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(18.0)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (location != null) {
            val marker = Marker(map)
            marker.apply {
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
            findNavController().popBackStack()
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