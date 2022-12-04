package com.example.turapp.utils.RecyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.turapp.R
import com.example.turapp.databinding.RvPoiItemBinding
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import java.text.SimpleDateFormat
import java.util.*

class MyPointListAdapter(private val onClickListener: OnClickListener, val context: Context):
    ListAdapter<MyPoint, MyPointListAdapter.MyPointViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPointViewHolder {
        return MyPointViewHolder(RvPoiItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false), context)
    }

    override fun onBindViewHolder(holder: MyPointViewHolder, position: Int) {
        val myPoint = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(myPoint)
        }
        holder.bind(myPoint)
    }

    companion object DiffCallback: DiffUtil.ItemCallback<MyPoint>() {
        override fun areItemsTheSame(oldItem: MyPoint, newItem: MyPoint): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MyPoint, newItem: MyPoint): Boolean {
            return oldItem.pointId == newItem.pointId
        }
    }


    class MyPointViewHolder(private var binding: RvPoiItemBinding, private var context: Context): RecyclerView.ViewHolder(binding.root) {
        fun bind(myPoint: MyPoint) {
            binding.myPoint = myPoint
            binding.rvDateView.text = convertLongToTime(myPoint.createdAt)
            val noImage = when (myPoint.type) {
                TYPE_POI -> R.drawable.ic_marker_blue
                TYPE_TRACKING -> R.drawable.ic_run_man
                TYPE_SNAPSHOT -> R.drawable.ic_camera
                else -> R.drawable.ic_baseline_broken_image
            }
            val drawable = context.getDrawable(noImage)?.apply {
                setTint(context.getColor(R.color.colorPrimary))
            }
            binding.rvIconView.setImageDrawable(drawable)
            binding.executePendingBindings()
        }
        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            return format.format(date)
        }
    }
    class OnClickListener(val clickListener: (myPoint: MyPoint) -> Unit) {
        fun onClick(myPoint: MyPoint) = clickListener(myPoint)
    }




}

