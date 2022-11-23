package com.example.turapp.utils.RecyclerViewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.turapp.R
import com.example.turapp.databinding.RvItemLocationBinding
import com.example.turapp.fragments.StartFragmentDirections
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import kotlinx.android.synthetic.main.rv_item_location.view.*

// BASED ON https://youtu.be/HtwDXRWjMcU

class LocationAdapter(
    var myPoints: List<MyPoint>,
    val nav: NavController
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(val binding: RvItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvItemLocationBinding.inflate(layoutInflater, parent, false)
        return LocationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return myPoints.size
    }

    //Take the data from each item and set it to each corresponding view
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.itemView.apply {
            val point = myPoints[position]
            btnLocation.apply {
                this.text = point.title
                when (point.type) {
                    TYPE_POI -> this.icon = resources.getDrawable(R.drawable.ic_marker_blue)
                    TYPE_SNAPSHOT -> this.icon = resources.getDrawable(R.drawable.ic_camera)
                    TYPE_TRACKING -> this.icon = resources.getDrawable(R.drawable.ic_run_man)
                    else -> this.icon = resources.getDrawable(R.drawable.ic_help)
                }
                setOnClickListener {
                    nav.navigate(
                        StartFragmentDirections.actionStartFragmentToPointOfInterestFragment(
                            point.pointId!!, point.type
                        )
                    )
                }
            }
        }
    }


}