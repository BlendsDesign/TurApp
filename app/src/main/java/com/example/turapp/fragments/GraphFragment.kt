package com.example.turapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.R
import com.example.turapp.databinding.FragmentGraphBinding
import com.example.turapp.repository.trackingDb.entities.MyPointWeek
import com.example.turapp.utils.helperFiles.GraphValueFormatter
import com.example.turapp.viewmodels.GraphViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis

class GraphFragment : Fragment() {

    private val viewModel: GraphViewModel by lazy {
        ViewModelProvider(
            this,
            GraphViewModel.Factory(requireActivity().application)
        )[GraphViewModel::class.java]
    }

    private lateinit var binding: FragmentGraphBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentGraphBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set up weeks spinner
        binding.spinnerWeek.apply {
            adapter = WeekAdapter().apply { setNotifyOnChange(false) }
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    viewModel.weeks.value?.getOrNull(position)?.let {
                        viewModel.currentWeek.value = it
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        // listen to weeks info from database and listen to it
        viewModel.weeks.observe(viewLifecycleOwner) {
            (binding.spinnerWeek.adapter as WeekAdapter).apply {
                clear()
                addAll(it)
                notifyDataSetChanged()
            }
        }

        // listen to new my points data and display it
        viewModel.rawData.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            binding.graphSteps.apply {
                data = viewModel.getStepsData()
                xAxis.apply {
                    axisMaximum = data.dataSetCount.toFloat() * 3
                    valueFormatter = GraphValueFormatter(it)
                    setCenterAxisLabels(true)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                axisLeft.apply { setDrawGridLines(false) }
                axisRight.isEnabled = false
                description = Description().apply {
                    text = "X-axis: Date\nY-axis: Steps"
                    setPosition(300f, 30f)
                    textAlign = Paint.Align.CENTER
                }
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        xAxis.textColor = Color.WHITE
                        axisLeft.textColor = Color.WHITE
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.WHITE}
                        legend.textColor = Color.WHITE
                        description.textColor = Color.WHITE
                    }
                    else -> {
                        xAxis.textColor = Color.BLACK
                        axisLeft.textColor = Color.BLACK
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.BLACK}
                        legend.textColor = Color.BLACK
                        description.textColor = Color.BLACK
                    }

                }
                invalidate()
            }
            binding.graphDistance.apply {
                data = viewModel.getDistanceData()
                xAxis.apply {
                    axisMaximum = data.dataSetCount.toFloat() * 3
                    valueFormatter = GraphValueFormatter(it)
                    setCenterAxisLabels(true)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    setDrawGridLines(false)
                }
                axisRight.isEnabled = false
                description = Description().apply {
                    text = "X-axis: Date\nY-axis: Distance (in meters)"
                    setPosition(300f, 30f)
                    textAlign = Paint.Align.CENTER
                }
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        xAxis.textColor = Color.WHITE
                        axisLeft.textColor = Color.WHITE
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.WHITE}
                        legend.textColor = Color.WHITE
                        description.textColor = Color.WHITE
                    }
                    else -> {
                        xAxis.textColor = Color.BLACK
                        axisLeft.textColor = Color.BLACK
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.BLACK}
                        legend.textColor = Color.BLACK
                        description.textColor = Color.BLACK
                    }

                }
                invalidate()
            }
            binding.graphTime.apply {
                data = viewModel.getTimeData()
                xAxis.apply {
                    axisMaximum = data.dataSetCount.toFloat() * 3
                    valueFormatter = GraphValueFormatter(it)
                    setCenterAxisLabels(true)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                axisLeft.apply { setDrawGridLines(false) }
                axisRight.isEnabled = false
                description = Description().apply {
                    text = "X-axis: Date\nY-axis: Time Taken (in minutes)"
                    setPosition(300f, 30f)
                    textAlign = Paint.Align.CENTER
                }
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        xAxis.textColor = Color.WHITE
                        axisLeft.textColor = Color.WHITE
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.WHITE}
                        legend.textColor = Color.WHITE
                        description.textColor = Color.WHITE
                    }
                    else -> {
                        xAxis.textColor = Color.BLACK
                        axisLeft.textColor = Color.BLACK
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.BLACK}
                        legend.textColor = Color.BLACK
                        description.textColor = Color.BLACK
                    }

                }
                invalidate()
            }
            binding.graphAscent.apply {
                data = viewModel.getAscentData()
                xAxis.apply {
                    axisMaximum = data.dataSetCount.toFloat() * 3
                    valueFormatter = GraphValueFormatter(it)
                    setCenterAxisLabels(true)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                axisLeft.apply { setDrawGridLines(false) }
                axisRight.isEnabled = false
                description = Description().apply {
                    text = "X-axis: Date\nY-axis: Total Ascent"
                    setPosition(300f, 30f)
                    textAlign = Paint.Align.CENTER
                }
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        xAxis.textColor = Color.WHITE
                        axisLeft.textColor = Color.WHITE
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.WHITE}
                        legend.textColor = Color.WHITE
                        description.textColor = Color.WHITE
                    }
                    else -> {
                        xAxis.textColor = Color.BLACK
                        axisLeft.textColor = Color.BLACK
                        data.dataSets.forEach { dataSet -> dataSet.valueTextColor = Color.BLACK}
                        legend.textColor = Color.BLACK
                        description.textColor = Color.BLACK
                    }

                }
                invalidate()
            }
        }
    }

    inner class WeekAdapter: ArrayAdapter<MyPointWeek>(
        requireContext(),
        android.R.layout.simple_list_item_1,
        android.R.id.text1,
        mutableListOf()
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(
                android.R.layout.simple_list_item_1, parent,false)
            view.findViewById<TextView>(android.R.id.text1).text =
                getString(R.string._week_item, getItem(position)?.week)
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
            getView(position, convertView, parent)
    }

}
