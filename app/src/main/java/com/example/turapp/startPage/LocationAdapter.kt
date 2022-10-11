package com.example.turapp.startPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.turapp.databinding.RvItemLocationBinding
import com.example.turapp.roomDb.entities.PointOfInterest
import kotlinx.android.synthetic.main.rv_item_location.view.*

// BASED ON https://youtu.be/HtwDXRWjMcU

class LocationAdapter(
    var locations: List<PointOfInterest>,
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
            val loc = locations[position]
            tvLocationName.text = loc.poiName + loc.poiId.toString()
            tvDistance.text = loc.poiLengt.toString()
            setOnClickListener {
                nav.navigate(
                    StartFragmentDirections.actionStartFragmentToPointOfInterestFragment(
                        loc.poiId?: -1
                    )
                )
            }
        }
    }


}