package com.example.turapp.startPage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.turapp.databinding.RvItemLocationBinding
import kotlinx.android.synthetic.main.rv_item_location.view.*

// BASED ON https://youtu.be/HtwDXRWjMcU

class LocationAdapter(
    var locations: List<Location>,
    val nav: NavController
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(val binding: RvItemLocationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvItemLocationBinding.inflate(layoutInflater,parent,false)
        return LocationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    //Take the data from each item and set it to each corresponding view
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.itemView.apply {
            tvLocationName.text = locations[position].title
            tvDistance.text = locations[position].distance.toString()
            setOnClickListener {
                nav.navigate(StartFragmentDirections.actionStartFragmentToPointOfInterestFragment(locations[position].title))
                Toast.makeText(context.applicationContext, locations[position].title, Toast.LENGTH_SHORT).show()
            }
        }
    }


}