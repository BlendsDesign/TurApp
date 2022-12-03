package com.example.turapp.fragments

import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.databinding.FragmentSaveMyPointBinding
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.helperFiles.NAVIGATION_ARGUMENT_SAVING_TYPE
import com.example.turapp.viewmodels.SaveMyPointViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.drawing.MapSnapshot
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*

class SaveMyPointFragment : Fragment() {

    private lateinit var binding: FragmentSaveMyPointBinding

    private lateinit var viewModel: SaveMyPointViewModel

    private var boundingBox: BoundingBox? = null

    private var location: GeoPoint? = null
    private lateinit var map: MapView

    private var marker: Marker? = null

    private var disableScrollView: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val typeArgument = it.getString(NAVIGATION_ARGUMENT_SAVING_TYPE)
            Log.d("SaveMyPointFragment TYPE", typeArgument ?: "null")
            if (typeArgument == null) {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            location = it.get("location") as GeoPoint?
            val imageUri: Uri? = it.get("uri") as Uri?
            if (typeArgument != null) {
                val app = requireNotNull(activity).application
                viewModel = ViewModelProvider(
                    this,
                    SaveMyPointViewModel.Factory(app, typeArgument, imageUri)
                )[SaveMyPointViewModel::class.java]
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
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

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

        viewModel.finishedSavingPoint.observe(viewLifecycleOwner) {
            if (it)
                findNavController().navigate(SaveMyPointFragmentDirections.actionSaveMyPointFragmentToTrackingFragment())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.typeArgument == TYPE_TRACKING) {
            viewModel.trackedLocations.observe(
                viewLifecycleOwner
            ) { outerList ->
                if (!outerList.isNullOrEmpty() && outerList.first().isNotEmpty()) {
                    drawTrackedLocations(outerList)
                }
            }

        } else if (location != null) {
            marker = Marker(map)
            marker?.apply {
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_blue)
                isDraggable = true
                lifecycleScope.launch {
                    location?.let { loc ->
                        title =  getLocationInformation(loc)
                    }
                }
                showInfoWindow()
                setOnMarkerDragListener(getMarkerDragListener())
                position = location
            }
            map.overlays.add(marker)
            if (viewModel.typeArgument == TYPE_POI) {
                binding.frameForMap.visibility = View.VISIBLE
                binding.btnGrpLocation.isChecked = true
            }
            map.controller.animateTo(location)
        }
        if (viewModel.typeArgument == TYPE_SNAPSHOT) {
            binding.imgHolder.visibility = View.VISIBLE
            binding.btnGrpImage.isChecked = true
        }

        binding.btnGrpImage.addOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                binding.imgHolder.visibility = View.VISIBLE
            } else {
                binding.imgHolder.visibility = View.GONE
            }
        }
        binding.btnGrpLocation.addOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                binding.frameForMap.visibility = View.VISIBLE
                map.addOnFirstLayoutListener { v, left, top, right, bottom ->
                    boundingBox?.let {
                        map.zoomToBoundingBox(it.increaseByScale(1.2F), false)
                        if (map.zoomLevelDouble > 20)
                            map.controller.setZoom(20.0)
                        Log.d("BOUNDINGBOX", map.zoomLevelDouble.toString())
                    }
                }


            } else {
                binding.frameForMap.visibility = View.GONE
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
        } catch (e: IndexOutOfBoundsException) {
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
                    lifecycleScope.launch {
                        marker.title = getLocationInformation(marker.position)
                    }
                    marker.showInfoWindow()
                    map.controller.animateTo(marker.position)
                    binding.scrollView.setScrollingEnabled(true)

                }
            }

            override fun onMarkerDragStart(marker: Marker?) {

            }

        }
    }

    private fun drawTrackedLocations(outerList: MutableList<MutableList<GeoPoint>>) {
        lifecycleScope.launch {
            if (outerList.isEmpty() || outerList.first().isEmpty())
                cancel("Empty or null list")

            val startPoint = outerList.first().first()
            var endPoint = startPoint
            try {
                endPoint = outerList.last().last()
            } catch (e: java.lang.IndexOutOfBoundsException) {
                e.printStackTrace()
            }

            val allPointsForBoundingBox = mutableListOf<GeoPoint>()

            // This draws the line or lines
            outerList.forEach { innerList ->
                allPointsForBoundingBox.addAll(innerList)
                val poli = Polyline()
                poli.color = Color.RED
                poli.setPoints(innerList)
                map.overlays.add(poli)
                map.invalidate()

            }

            // Startingpoint marker
            marker = Marker(map)
            marker?.apply {
                isDraggable = false
                title = "Start"
                icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_marker_orange)
                subDescription = getLocationInformation(startPoint)
                showInfoWindow()
                position = startPoint
            }
            val endMarker = Marker(map)
            endMarker.apply {
                isDraggable = false
                title = "End"
                icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_marker_blue)
                subDescription = getLocationInformation(endPoint)
                showInfoWindow()
                position = endPoint
            }
            if (endPoint.distanceToAsDouble(startPoint) < 3) {
                endMarker.title = "Start/End"
                endMarker.showInfoWindow()
            }
            map.overlays.apply {
                add(marker)
                add(endMarker)
            }
            binding.frameForMap.visibility = View.VISIBLE
            binding.btnGrpLocation.isChecked = true
            boundingBox = BoundingBox.fromGeoPointsSafe(allPointsForBoundingBox)


//            if (boundingBox.diagonalLengthInMeters < 1000)
//                map.controller.setZoom(20.0)
            Log.d("MAP ZOOMLEVEL", map.zoomLevelDouble.toString())

            map.invalidate()
        }
    }

    fun takeMapSnapshot() {
        //val mapSnapshot =
        MapSnapshot(MapSnapshot.MapSnapshotable() {
                                                  //TODO Do something with the snapshot
        }, MapSnapshot.INCLUDE_FLAG_UPTODATE, map)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.finishedSavingPoint.value != true)
            viewModel.deleteTakenPicture()
    }
}