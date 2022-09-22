package com.example.turapp.startPage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.R
import com.example.turapp.databinding.StartFragmentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class StartFragment : Fragment() {

    private val viewModel: StartViewModel by lazy {
        val test = "Her er HOME med en LISTE AV Points of Interest"
        ViewModelProvider(this, StartViewModel.Factory(test))[StartViewModel::class.java]
    }

    private lateinit var binding: StartFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding = StartFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // SETTING UP BOTTOM NAV
        val bottomNav = this.activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.miCamera -> Toast.makeText(this.requireContext(), "CAMERA PRESSED", Toast.LENGTH_SHORT).show()
                R.id.miMap -> {
                    val navCon = findNavController()
                    navCon.popBackStack(R.id.startFragment, false)
                    navCon.navigate(StartFragmentDirections.actionStartFragmentToLiveSensorDataFragment())
                }
                R.id.miLiveSensors -> {
                    val navCon = findNavController()
                    navCon.popBackStack(R.id.startFragment, false)
                    navCon.navigate(StartFragmentDirections.actionStartFragmentToLiveSensorDataFragment())
                }
            }
            true
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvLocations.apply {
            adapter = LocationAdapter(viewModel.getMockData())
            layoutManager = LinearLayoutManager(context)
        }
    }
}