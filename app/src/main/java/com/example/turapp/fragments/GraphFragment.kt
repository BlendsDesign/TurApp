package com.example.turapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.R
import com.example.turapp.databinding.FragmentGraphBinding
import com.example.turapp.repository.trackingDb.entities.MyPointWeek
import com.example.turapp.viewmodels.GraphViewModel
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GraphFragment : Fragment() {

    enum class Metrics {
        STEPS,
        DISTANCE,
        TIME_TAKEN
    }

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

        // set-up metrics options
        binding.spinnerMetrics.adapter = MetricsAdapter()

        // listen to metrics changes
        (binding.spinnerMetrics.adapter as MetricsAdapter).setOnCheckChangedListener(
            object: OnCheckChangedListener {
                override fun onCheckChanged(metric: Metrics, checked: Boolean) {
                    when (metric) {
                        Metrics.STEPS -> {
                            viewModel.showSteps.value = checked
                        }
                        Metrics.DISTANCE -> {
                            viewModel.showDistance.value = checked
                        }
                        Metrics.TIME_TAKEN -> {
                            viewModel.showTimeTaken.value = checked
                        }
                    }
                }
            }
        )

        // listen to new my points data and display it
        viewModel.rawData.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            binding.graphSteps.apply {
                data = viewModel.getBarData()
                groupBars(0f, 0.1f, 0.0f)
                xAxis.apply {
                    axisMaximum = data.dataSetCount.toFloat() * 3
                    valueFormatter = object: ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            if (value.toInt().toFloat() != value) {
                                return ""
                            }
                            return it.getOrNull(value.toInt())
                                ?.createdAt?.let { date ->
                                    DateTimeFormatter.ofPattern("dd/MMM").format(
                                        ZonedDateTime.ofInstant(Date(date).toInstant(), ZoneId.systemDefault()))
                                } ?: ""
                        }
                    }
                    setCenterAxisLabels(true)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                axisLeft.apply { setDrawGridLines(false) }
                axisRight.isEnabled = false
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        xAxis.textColor = Color.WHITE
                        axisLeft.textColor = Color.WHITE
                    }
                    else -> {
                        xAxis.textColor = Color.BLACK
                        axisLeft.textColor = Color.BLACK
                    }

                }
                description = Description().apply { isEnabled = false }
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

    inner class MetricsAdapter: ArrayAdapter<Pair<Metrics, Boolean>>(
        requireContext(),
        android.R.layout.simple_list_item_1,
        android.R.id.text1
    ) {

        private var onCheckChangedListener: OnCheckChangedListener? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(
                R.layout.item_metric_checkable, parent,false).also {
                it.findViewById<CheckBox>(R.id.check1).apply {
                    getItem(position).let { item ->
                        isChecked = item.second
                        setOnCheckedChangeListener { _, isChecked ->
                            onCheckChangedListener?.onCheckChanged(item.first, isChecked)
                        }
                    }
                }
            }
            view.findViewById<TextView>(R.id.text1).apply {
                getItem(position).let { item ->
                    text = item.first.toString()
                }
            }
            return view
        }

        override fun getItem(position: Int): Pair<Metrics, Boolean> {
            return when (position) {
                0 -> Pair(Metrics.STEPS, viewModel.showSteps.value ?: true)
                1 -> Pair(Metrics.DISTANCE, viewModel.showDistance.value ?: true)
                2 -> Pair(Metrics.TIME_TAKEN, viewModel.showTimeTaken.value ?: true)
                else -> Pair(Metrics.STEPS, viewModel.showSteps.value ?: true)
            }
        }

        override fun getCount() = 3

        fun setOnCheckChangedListener(onCheckChangedListener: OnCheckChangedListener) {
            this.onCheckChangedListener = onCheckChangedListener
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
            getView(position, convertView, parent)

    }
    interface OnCheckChangedListener {
        fun onCheckChanged(metric: Metrics, checked: Boolean)
    }
}
