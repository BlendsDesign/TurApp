package com.example.turapp.startPage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.databinding.StartFragmentBinding

class StartFragment : Fragment() {

    private val viewModel: StartViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, StartViewModel.Factory(app))[StartViewModel::class.java]
    }

    private lateinit var binding: StartFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = StartFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if(!it) {
                binding.rvLocations.apply {
                    val navControl = findNavController()
                    adapter = LocationAdapter(viewModel.points.value ?: listOf(), navControl)
                    layoutManager = LinearLayoutManager(context)
                }
                binding.statusImage.visibility = View.GONE
            } else {
                binding.statusImage.visibility = View.VISIBLE
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshList()
    }
}