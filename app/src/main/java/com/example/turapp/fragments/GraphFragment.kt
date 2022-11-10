package com.example.turapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.databinding.FragmentGraphBinding
import com.example.turapp.viewmodels.GraphViewModel
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GraphFragment : Fragment() {

    private val viewModel: GraphViewModel by lazy {
        ViewModelProvider(
            this,
            GraphViewModel.Factory()
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

        viewModel.data.observe(viewLifecycleOwner) {
            binding.graphSteps.apply {
                data = it
                groupBars(0f, 0.1f, 0.0f)
                xAxis.apply {
                    axisMaximum = it.dataSetCount.toFloat() * 3
                    valueFormatter = object: ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            if (value.toInt().toFloat() != value) {
                                return ""
                            }
                            return viewModel.rawData.value?.getOrNull(value.toInt())
                                ?.createdAt?.let { date ->
                                    DateTimeFormatter.ofPattern("dd/MMM").format(
                                        ZonedDateTime.ofInstant(Date(date).toInstant(), ZoneId.systemDefault()))
                                } ?: ""
                        }
                    }
                    setCenterAxisLabels(true)
                    position = XAxis.XAxisPosition.BOTTOM
                }
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
                invalidate()
            }
        }
    }


}