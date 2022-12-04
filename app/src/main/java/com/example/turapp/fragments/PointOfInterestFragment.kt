package com.example.turapp.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.location.Geocoder
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
import com.example.turapp.viewmodels.PointOfInterestViewModel
import com.example.turapp.databinding.FragmentPointOfInterestBinding
import com.example.turapp.repository.trackingDb.entities.*
import com.github.mikephil.charting.data.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PointOfInterestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var _binding: FragmentPointOfInterestBinding
    private val binding get() = _binding

    private lateinit var map: MapView

    private lateinit var viewModel: PointOfInterestViewModel

    private lateinit var marker: Marker

    private var boundingBox: BoundingBox? = null

    private var graphEntries = mutableListOf<MutableList<Entry>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = requireNotNull(activity).application
        arguments?.let {
            val id = it.getLong("id")
            if (id != 0L) {
                viewModel = ViewModelProvider(
                    this,
                    PointOfInterestViewModel.Factory(app, id)
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
        map = binding.mapHolder
        marker = Marker(map)
        marker.isDraggable = false
        map.overlays.add(marker)
        lifecycleScope.launchWhenCreated {
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT) //3
            map.apply {
                setMultiTouchControls(true) //3
                controller.setZoom(18.0)
            }
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
                map.addOnFirstLayoutListener { _, _, _, _, _ ->
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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.trek.observe(viewLifecycleOwner) {
            if (it != null) {
                it.trekList?.let { outerlist ->
                    binding.graphAltitude.visibility = View.VISIBLE
                    drawTrackedLocations(outerlist)
                    getTrekAltitudes(outerlist)
                }
            } else {
                binding.graphAltitude.visibility = View.GONE
            }

        }


        viewModel.myPoint.observe(viewLifecycleOwner) { myPoint ->
            if (myPoint != null) {
                myPoint.location?.let {
                    marker.position = it
                    map.controller.setCenter(it)
                    marker.title = getLocationInformation(it)
                    marker.setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        true
                    }
                }
                lifecycleScope.launch {
                    binding.apply {
                        titleInputField.setText(myPoint.title ?: " ")
                        dateInputField.setText(convertLongToTime(myPoint.createdAt))
                        if (myPoint.description.isNullOrBlank())
                            descInputFieldHolder.visibility = View.GONE
                        else
                            descInputField.setText(myPoint.description)
                    }
                    when (myPoint.type) {
                        TYPE_SNAPSHOT -> {
                            binding.apply {
                                if (!myPoint.image.isNullOrBlank()) {
                                    btnGrpImage.isChecked = true
                                    imgHolder.visibility = View.VISIBLE
                                    frameForMap.visibility = View.GONE
                                } else {
                                    btnGrpImage.visibility = View.GONE
                                    imgHolder.visibility = View.GONE
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.no_snapshot_image), Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            marker.apply {
                                icon = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_image
                                )
                                icon.setTint(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.theme_blue
                                    )
                                )
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            binding.loadingScreen.visibility = View.GONE
                        }
                        TYPE_POI -> {
                            binding.apply {
                                if (myPoint.location != null) {
                                    marker.apply {
                                        icon = ContextCompat.getDrawable(
                                            requireContext(),
                                            R.drawable.ic_marker_blue
                                        )
                                    }
                                    btnGrpLocation.isChecked = true
                                    frameForMap.visibility = View.VISIBLE
                                } else {
                                    btnGrpLocation.visibility = View.GONE
                                    frameForMap.visibility = View.GONE
                                }
                                loadingScreen.visibility = View.GONE
                            }
                        }
                        TYPE_TRACKING -> {
                            marker.apply {
                                icon = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_run_circle_blue
                                )
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            binding.otherInfoInputField.setText(getActivityInformationString(myPoint))
                            binding.otherInfoInputFieldHolder.visibility = View.VISIBLE
                        }
                    }

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
                            binding.loadingScreen.visibility = View.VISIBLE
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
            }
        }


        viewModel.finishedDeleting.observe(viewLifecycleOwner)
        {
            if (it) {
                findNavController().popBackStack()
            }
        }
        return binding.root
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

            map.invalidate()
            binding.loadingScreen.visibility = View.GONE
        }
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
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getActivityInformationString(point: MyPoint?): String {
        if (point == null)
            return ""
        val distance = point.distanceInMeters ?: 0f
        val timeInSeconds = point.timeTaken?.div(1000.0) ?: 1.0
        val speed = distance.div(timeInSeconds)
        return String.format(
            getString(R.string.tracking_info_format_string),
            distance,
            timeInSeconds,
            speed
        )
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

    private fun getTrekAltitudes(outerList: MutableList<MutableList<GeoPoint>>) {
        lifecycleScope.launch {
            var distance = 0.0
            var altitude = 0.0
            var last: GeoPoint? = null
            outerList.forEach { innerList ->
                graphEntries.add(mutableListOf())
                for (gp in innerList) {
                    last?.let {
                        distance += it.distanceToAsDouble(gp)
                        altitude += it.altitude - gp.altitude
                    }
                    graphEntries.last().add(Entry(distance.toFloat(), altitude.toFloat()))
                    Log.d("GraphEntry test", graphEntries.toString())
                    last = gp
                }
                last = null
            }
            setUpGraph()
            binding.graphAltitude.visibility = View.VISIBLE
        }
    }

    private fun setUpGraph() {
        binding.graphAltitude.apply {
            data = LineData().apply {

            }

            for (outerlist in graphEntries) {
                this.data.addDataSet(LineDataSet(outerlist, "").apply {
                    color = ContextCompat.getColor(requireContext(),R.color.theme_blue)
                    lineWidth = 1.5f
                })
            }
            invalidate()

        }
    }
}