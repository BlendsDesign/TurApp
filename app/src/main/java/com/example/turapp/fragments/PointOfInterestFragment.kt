package com.example.turapp.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.viewmodels.PointOfInterestViewModel
import com.example.turapp.utils.RecyclerViewAdapters.RecordingListAdapter
import com.example.turapp.databinding.FragmentPointOfInterestBinding
import com.example.turapp.repository.trackingDb.entities.*
import com.example.turapp.repository.trackingDb.relations.MyPointWithGeo
import com.example.turapp.roomDb.TypeOfPoint
import com.example.turapp.roomDb.entities.RecordedActivity
import kotlinx.android.synthetic.main.fragment_save_my_point.*
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
            val id = it.getInt("id")
            val type = it.getString("type")
            if (id != null && type != null) {
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
            findNavController().navigate(PointOfInterestFragmentDirections.actionPointOfInterestFragmentToStartFragment())
        }


        viewModel.myPoint.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                //Set up POI textviews
                binding.apply {
                    titleInputField.setText(it.point.title ?: " ")
                    dateInputField.setText(
                        String.format("${Date(Timestamp(it.point.createdAt).time)}")
                    )
                    descInputField.setText(it.point.description)
                    btnDeleteMyPointOrSaveEdits.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(context).create()
                        if (viewModel?.isInEditMode?.value != true) {
                            alertDialog.setTitle(getString(R.string.delete_are_you_sure))
                            alertDialog.setButton(
                                AlertDialog.BUTTON_POSITIVE,
                                "Yes"
                            ) { dialog: DialogInterface, _: Int ->
                                viewModel?.deletePoi()
                            }
                        } else {
                            alertDialog.setTitle("Are you sure you want to save these changes")
                            alertDialog.setButton(
                                AlertDialog.BUTTON_POSITIVE,
                                "Yes"
                            ) { dialog: DialogInterface, _: Int ->
                                viewModel?.saveEdits()
                            }
                        }
                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Yes"
                        ) { dialog: DialogInterface, _: Int ->
                            viewModel?.deletePoi()
                        }
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEUTRAL,
                            "No"
                        ) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        alertDialog.show()
                    }
                }
                // Set up map
                if (it.geoData.isNotEmpty())
                    setUpMap()
            }
        })

        viewModel.finishedDeleting.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().popBackStack()
            }
        })

        return binding.root
    }

    private fun setUpMap(): Boolean {
        Toast.makeText(requireContext(), "setUpMap() ran", Toast.LENGTH_SHORT).show()
        val myPointWithGeo = viewModel.myPoint.value
        if (myPointWithGeo == null || myPointWithGeo.geoData.isEmpty())
            return false
        lifecycleScope.launch {
            map = binding.mapHolder
            map.setTileSource(TileSourceFactory.MAPNIK)
            val geoPoints = mutableListOf<GeoPoint>()
            val marker = Marker(map)
            if (myPointWithGeo.point.type == TYPE_TRACKING) {
                val geoPointsForBoundingBox = mutableListOf<GeoPoint>()
                val endMarker = Marker(map)
                myPointWithGeo.geoData.forEach {

                    // Use this to
                    geoPoints.add(it.geoPoint)
                    geoPointsForBoundingBox.add(it.geoPoint)
                    // Check if it is a starting or pause point
                    when (it.type) {
                        TRACKING_STARTING_POINT -> {
                            marker.apply {
                                icon = AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_marker_blue
                                )
                                position = geoPoints[0]
                                title = "Starting Point"
                                subDescription =
                                    getActivityInformationString(viewModel.myPoint.value?.point)
                                showInfoWindow()
                            }
                        }
                        TRACKING_END_POINT -> {
                            endMarker.apply {
                                icon = AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_marker_blue
                                )
                                position = geoPoints[0]
                                title = "Starting Point"
                                subDescription =
                                    getActivityInformationString(viewModel.myPoint.value?.point)
                                showInfoWindow()
                            }
                        }
                        TRACKING_PAUSE_POINT -> {
                            val poli = Polyline()
                            poli.color = Color.RED
                            geoPoints.forEach {
                                poli.addPoint(it)
                            }
                            map.overlays.add(poli)
                            geoPoints.clear()
                        }
                    }
                }
                map.overlays.add(endMarker)
                map.zoomToBoundingBox(
                    BoundingBox.fromGeoPointsSafe(geoPointsForBoundingBox),
                    true
                )
            } else {
                marker.apply {
                    icon = if (myPointWithGeo.point.type == TYPE_POI) {
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
                    position = myPointWithGeo.geoData.first().geoPoint
                    title = getLocationInformation(myPointWithGeo.geoData.first().geoPoint)
                    showInfoWindow()
                }
                map.controller.apply {
                    setZoom(18)
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
        val timeInSeconds = point.timeTaken?.div(100.0) ?: 1.0
        val speed = distance.div(timeInSeconds)
        return String.format(
            "You ran %.2f meters in %.2f seconds at a speed of %.2f m/s",
            distance,
            timeInSeconds,
            speed
        )
    }
}