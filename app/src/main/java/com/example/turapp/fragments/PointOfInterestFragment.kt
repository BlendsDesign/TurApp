package com.example.turapp.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.viewmodels.PointOfInterestViewModel
import com.example.turapp.databinding.FragmentPointOfInterestBinding
import com.example.turapp.repository.trackingDb.entities.*
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.sql.Timestamp
import java.util.*


class PointOfInterestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var _binding: FragmentPointOfInterestBinding
    private val binding get() = _binding

    private lateinit var map: MapView

    private lateinit var viewModel: PointOfInterestViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = requireNotNull(activity).application
        arguments?.let {
            val id = it.getLong("id")
            val type = it.getString("type")
            if (id != 0L && type != null) {
                viewModel = ViewModelProvider(
                    this,
                    PointOfInterestViewModel.Factory(app, id, type)
                )[PointOfInterestViewModel::class.java]
            } else
                findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPointOfInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.trek.observe(viewLifecycleOwner) { outerList ->
            var maxHeight = 0.0
            try {
                outerList.trekList.forEach { innerList ->
                    innerList.forEach {
                        if (it.altitude > maxHeight)
                            maxHeight = it.altitude
                    }
                }
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
            Toast.makeText(requireContext(), "${maxHeight}", Toast.LENGTH_SHORT).show()
        }


        viewModel.myPoint.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(requireContext(), it.totalAscent.toString(), Toast.LENGTH_SHORT).show()
                //Set up POI textviews
                Toast.makeText(requireContext(), "${it.location?.altitude}", Toast.LENGTH_SHORT)
                    .show()
                binding.apply {
                    titleInputField.setText(it.title ?: " ")
                    dateInputField.setText(
                        String.format("${Date(Timestamp(it.createdAt).time)}")
                    )
                    descInputField.setText(it.description)
                }
                binding.btnDeleteMyPointOrSaveEdits.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(context).create()
                    if (viewModel.isInEditMode.value != true) {
                        alertDialog.setTitle(getString(R.string.delete_are_you_sure))
                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Yes"
                        ) { dialog: DialogInterface, _: Int ->
                            Log.d("DELETE", "ALERTDIALOG")
                            viewModel.deletePoi()
                        }
                    } else {
                        alertDialog.setTitle("Are you sure you want to save these changes")
                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Yes"
                        ) { dialog: DialogInterface, _: Int ->
                            viewModel.saveEdits()
                        }
                    }
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEUTRAL,
                        "No"
                    ) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    alertDialog.show()
                }
                // Set up map
                if (it.location != null)
                    setUpMap()

                if (it.type == TYPE_TRACKING)
                    binding.otherInfoInputField.setText(getActivityInformationString(it))
            }
        }


        viewModel.finishedDeleting.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    private fun setUpMap(): Boolean {
        Toast.makeText(requireContext(), "setUpMap() ran", Toast.LENGTH_SHORT).show()
        val myPoint = viewModel.myPoint.value
        if (myPoint?.location == null)
            return false
        lifecycleScope.launch {
            map = binding.mapHolder
            map.setTileSource(TileSourceFactory.MAPNIK)
            val geoPoints = mutableListOf<GeoPoint>()
            val marker = Marker(map)
            if (myPoint.type == TYPE_TRACKING) {
//                val geoPointsForBoundingBox = mutableListOf<GeoPoint>()
//                val endMarker = Marker(map)
//                myPoint.geoData.forEach {
//
//                    // Use this to
//                    geoPoints.add(it.geoPoint)
//                    geoPointsForBoundingBox.add(it.geoPoint)
//                    // Check if it is a starting or pause point
//                    when (it.type) {
//                        TRACKING_STARTING_POINT -> {
//                            marker.apply {
//                                icon = AppCompatResources.getDrawable(
//                                    requireContext(),
//                                    R.drawable.ic_marker_blue
//                                )
//                                position = geoPoints[0]
//                                title = "Starting Point"
//                                subDescription =
//                                    getActivityInformationString(viewModel.myPoint.value?.point)
//                                showInfoWindow()
//                            }
//                        }
//                        TRACKING_END_POINT -> {
//                            endMarker.apply {
//                                icon = AppCompatResources.getDrawable(
//                                    requireContext(),
//                                    R.drawable.ic_marker_blue
//                                )
//                                position = geoPoints[0]
//                                title = "Starting Point"
//                                subDescription =
//                                    getActivityInformationString(viewModel.myPoint.value?.point)
//                                showInfoWindow()
//                            }
//                        }
//                        TRACKING_PAUSE_POINT -> {
//                            val poli = Polyline()
//                            poli.color = Color.RED
//                            geoPoints.forEach {
//                                poli.addPoint(it)
//                            }
//                            map.overlays.add(poli)
//                            geoPoints.clear()
//                        }
//                    }
//                }
//                map.overlays.add(endMarker)
//                map.zoomToBoundingBox(
//                    BoundingBox.fromGeoPointsSafe(geoPointsForBoundingBox),
//                    true
//                )
            } else {
                marker.apply {
                    icon = if (myPoint.type == TYPE_POI) {
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_marker_blue
                        )
                    } else {
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_camera
                        )
                    }
                    position = myPoint.location
                    title = getLocationInformation(myPoint.location)
                    showInfoWindow()
                }
                map.controller.apply {
                    setZoom(18.0)
                    animateTo(marker.position)
                }
            }
            map.overlays.add(marker)
        }
        binding.mapHolder.visibility = View.VISIBLE
        binding.btnGrpLocation.visibility = View.VISIBLE


        return true
    }

    private fun getLocationInformation(geoPoint: GeoPoint?): String {
        if (geoPoint == null) return "No Location Data"
        val gc = Geocoder(requireContext(), Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getActivityInformationString(point: MyPoint?): String {
        if (point == null)
            return ""
        val distance = point.distanceInMeters ?: 0f
        val timeInSeconds = point.timeTaken?.div(10.0) ?: 1.0
        val speed = distance.div(timeInSeconds)
        return String.format(
            "You ran %.2f meters in %.2f seconds at a speed of %.2f m/s",
            distance,
            timeInSeconds,
            speed
        )
    }
}