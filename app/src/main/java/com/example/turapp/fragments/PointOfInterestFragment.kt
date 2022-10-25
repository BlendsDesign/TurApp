package com.example.turapp.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.R
import com.example.turapp.viewmodels.PointOfInterestViewModel
import com.example.turapp.utils.RecyclerViewAdapters.RecordingListAdapter
import com.example.turapp.databinding.FragmentPointOfInterestBinding
import com.example.turapp.roomDb.TypeOfPoint
import com.example.turapp.roomDb.entities.RecordedActivity
import java.io.IOException
import java.sql.Timestamp
import java.util.*


class PointOfInterestFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var id: Int? = null
    private var type: TypeOfPoint? = null

    private lateinit var _binding: FragmentPointOfInterestBinding
    private val binding get() = _binding

    private lateinit var viewModel: PointOfInterestViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = requireNotNull(activity).application
        arguments?.let {
            id = it.getInt("id")
            type = it.get("type") as TypeOfPoint
        }
        if (id != null && type != null) {
            viewModel = ViewModelProvider(this, PointOfInterestViewModel.Factory(app, id!!, type!!))
                .get(PointOfInterestViewModel::class.java)
        } else
            findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPointOfInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.poi.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                //Set up POI textviews
                binding.apply {
                    tvPoiName.text = it.poi.poiName
                    tvPoiDate.text =
                        String.format("Recorded at: ${Date(Timestamp(it.poi.createdAt).time)}")
                    val loc = getLocationInformation(it.poi.poiLat, it.poi.poiLng)
                    tvPoiLength.text = String.format("Location: ${loc}")
                    tvTotalPoiWithRecordingsSize.text = String.format("Total size: ${it.toString().toByteArray().size} bytes")
                    btCloseRecordingView.setOnClickListener {
                        binding.showRecordingView.visibility = View.GONE
                    }
                    btDelete.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(context).create()
                        alertDialog.setTitle(getString(R.string.delete_are_you_sure))
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") {
                                dialog: DialogInterface, _: Int -> viewModel.deletePoi()
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No") {
                                dialog: DialogInterface, _: Int -> dialog.dismiss()
                        }
                        alertDialog.show()
                    }
                }
                binding.rvRecordings.apply {
                    val linear = binding.showRecordingView
                    val tv = binding.tvRecording
                    adapter = RecordingListAdapter(it.recording, linear, tv)
                }
            }
        })
        viewModel.activity.observe(viewLifecycleOwner, Observer { act ->
            if (act != null) {
                //Set up POI textviews
                binding.apply {
                    tvPoiName.text = act.activity.title
                    tvPoiDate.text =
                        String.format("Recorded at: ${Date(Timestamp(act.activity.timestamp).time)}")
                    val loc = getLocationInformation(act.activity.startingLat, act.activity.startingLng)
                    tvPoiLength.text = String.format("Location: ${loc}")
                    tvTotalPoiWithRecordingsSize.text = getActivityInformationString(act.activity)
                    btCloseRecordingView.setOnClickListener {
                        binding.showRecordingView.visibility = View.GONE
                    }
                    btDelete.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(context).create()
                        alertDialog.setTitle(getString(R.string.delete_are_you_sure))
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") {
                                dialog: DialogInterface, _: Int -> viewModel.deletePoi()
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No") {
                                dialog: DialogInterface, _: Int -> dialog.dismiss()
                        }
                        alertDialog.show()
                    }
                }
            }
        })


        viewModel.loadingImage.observe(viewLifecycleOwner, Observer {
            if(it) {
                binding.statusImage.visibility = View.VISIBLE
            } else {
                binding.statusImage.visibility = View.GONE
            }
        })

        viewModel.finishedDeleting.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().popBackStack()
            }
        })

        return binding.root
    }
    private fun getLocationInformation(lat: Float?, lng: Float?): String {
        if(lat == null || lng == null) return "No Location Data"
        val gc = Geocoder(requireContext(), Locale.getDefault())
        try {
            val adrs = gc.getFromLocation(lat.toDouble(), lng.toDouble(), 1)
            val ads = adrs!![0]
            return ads.getAddressLine(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
    private fun getActivityInformationString(act : RecordedActivity): String {
        val distance = act.totalDistance ?: 0
        val timeInSeconds: Double = act.timeInMillis / 1000.0
        val speed = distance.div(timeInSeconds)
        return String.format(
            "You ran %d meters in %.2f seconds at a speed of %.2f m/s", distance, timeInSeconds, speed
        )
    }
}