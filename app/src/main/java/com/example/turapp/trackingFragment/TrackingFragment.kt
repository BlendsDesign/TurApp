package com.example.turapp.trackingFragment

import android.content.ContextWrapper
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.turapp.BuildConfig
import com.example.turapp.Helper
import com.example.turapp.R
import com.example.turapp.databinding.FragmentTrackingBinding
import org.osmdroid.views.MapView


class trackingFragment : Fragment() {

    private lateinit var binding: FragmentTrackingBinding

    private lateinit var map: MapView // 3


    private val viewModel: TrackingViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, TrackingViewModel.Factory(app))[TrackingViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Helper.suggestedFix(contextWrapper = ContextWrapper(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}