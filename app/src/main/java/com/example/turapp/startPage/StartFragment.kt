package com.example.turapp.startPage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.databinding.StartFragmentBinding

class StartFragment : Fragment() {

    private val viewModel: StartViewModel by lazy {
        val test = "Her er HOME med en LISTE AV Points of Interest"
        ViewModelProvider(this, StartViewModel.Factory(test))[StartViewModel::class.java]
    }

    private var _binding: StartFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = StartFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        //setUpBottomNav()

        // SETTING UP BOTTOM NAV



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvLocations.apply {
            val navControl = findNavController()
            adapter = LocationAdapter(viewModel.getMockData(), navControl)
            layoutManager = LinearLayoutManager(context)
        }
    }

//    private fun setUpBottomNav() {
//        val bottomNav = this.activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
//        val navCon = findNavController()
//        bottomNav?.setOnItemSelectedListener {
//            when(it.itemId) {
//                R.id.miList -> navCon.popBackStack(R.id.startFragment, false)
//                R.id.miCamera -> {
//                    navCon.popBackStack(R.id.startFragment, false)
//                    navCon.navigate(StartFragmentDirections.actionStartFragmentToCameraFragment())
//                }
//                R.id.miMap -> {
//                    navCon.popBackStack(R.id.startFragment, false)
//                    navCon.navigate(StartFragmentDirections.actionStartFragmentToMapFragment())
//                }
//                R.id.miLiveSensors -> {
//                    navCon.popBackStack(R.id.startFragment, false)
//                    navCon.navigate(StartFragmentDirections.actionStartFragmentToLiveSensorDataFragment())
//                }
//            }
//            true
//        }
//
//    }

}