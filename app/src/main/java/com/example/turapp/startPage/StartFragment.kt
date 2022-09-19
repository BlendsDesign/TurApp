package com.example.turapp.startPage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.turapp.R
import com.example.turapp.databinding.StartFragmentBinding

class StartFragment : Fragment(), MenuProvider {


    private val viewModel: StartViewModel by lazy {
        val test = "velkommen til appen"
        ViewModelProvider(this, StartViewModel.Factory(test)).get(StartViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val binding = StartFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        when (menuItem.itemId) {
            R.id.miSettings -> {
                Toast.makeText(activity,"You clicked on settings", Toast.LENGTH_SHORT).show()
            }
            R.id.miHelp -> {
                Toast.makeText(activity,"You clicked on help", Toast.LENGTH_SHORT).show()
            }
            R.id.miInfo -> {
                Toast.makeText(activity,"You clicked on info", Toast.LENGTH_SHORT).show()
            }
            R.id.miClose -> {
                activity?.finish() //shuts down the app
            }

        }
        return true
    }


}