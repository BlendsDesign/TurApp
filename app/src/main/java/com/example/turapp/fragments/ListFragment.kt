package com.example.turapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.turapp.databinding.FragmentListBinding
import com.example.turapp.utils.RecyclerViewAdapters.MyPointListAdapter
import com.example.turapp.viewmodels.ListViewModel
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private lateinit var adapter: MyPointListAdapter

    private val viewModel: ListViewModel by lazy {
        val app = requireNotNull(activity).application
        ViewModelProvider(this, ListViewModel.Factory(app))[ListViewModel::class.java]
    }

    private lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater)

        adapter = MyPointListAdapter(MyPointListAdapter.OnClickListener {
            viewModel.setNavigateToMyPoint(it)
        }, requireContext())

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = viewModel
            rvMyPoints.layoutManager = LinearLayoutManager(requireContext())
            rvMyPoints.adapter = adapter
        }

        viewModel.points.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.allPoints.observe(viewLifecycleOwner) {
            viewModel.filterList()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it && viewModel.points.value?.isEmpty() == true) {
                binding.startFragmentListIsEmpty.visibility = View.VISIBLE
                binding.statusImage.visibility = View.GONE
            } else if (!it) {
                lifecycleScope.launch {
                    binding.statusImage.visibility = View.GONE
                    binding.startFragmentListIsEmpty.visibility = View.GONE
                }
            } else {
                binding.statusImage.visibility = View.VISIBLE
            }
        }

        viewModel.navigateToSelectedMyPoint.observe(viewLifecycleOwner) {
            if (it != null) {
                this.findNavController().navigate(
                    ListFragmentDirections.actionListFragmentToPointOfInterestFragment(
                        it.pointId!!
                    )
                )
                viewModel.navigateComplete()
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }
}