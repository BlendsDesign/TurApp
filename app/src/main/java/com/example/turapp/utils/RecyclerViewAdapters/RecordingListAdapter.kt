package com.example.turapp.utils.RecyclerViewAdapters

import android.hardware.Sensor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.turapp.databinding.RvRecordingItemBinding
import com.example.turapp.roomDb.entities.Recording
import kotlinx.android.synthetic.main.rv_recording_item.view.*

class RecordingListAdapter(
    var recordings: List<Recording>,
    val showRecordingView: LinearLayout,
    val textView: TextView
) : RecyclerView.Adapter<RecordingListAdapter.RecordingListViewHolder>() {

    inner class RecordingListViewHolder(val binding: RvRecordingItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvRecordingItemBinding.inflate(layoutInflater,parent,false)
        Log.i("RV", "onCreateViewHolder")
        return RecordingListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        Log.i("RV", recordings.size.toString())
        return recordings.size
    }

    //Take the data from each item and set it to each corresponding view
    override fun onBindViewHolder(holder: RecordingListViewHolder, position: Int) {
        holder.itemView.apply {
            val rec = recordings[position]
            Log.i("RV", rec.toString())
            tvSensorName.text = getSensorName(rec.sensorType)
            tvRecordingSize.text = String.format("Events recorded: ${rec.recording.size}    Size: ${rec.recording.toString().toByteArray().size} bytes")
            tvSensorName.setOnClickListener {
                showRecordingView.visibility = View.VISIBLE
                textView.text = getSensorDataXYZstring(rec)
            }
        }
    }
    private fun getSensorDataXYZstring(rec: Recording): String {
        var res = ""
        var counter = 1

        rec.recording.forEach {
            res = res + String.format("Data# ${counter++} :  X: ${it[0]} Y: ${it[1]} Z: ${it[2]}\n")
        }

        return res
    }

    private fun getSensorName(sensorType: Int): String {
        return when(sensorType) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer Recording"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope Recording"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer Recording"
            Sensor.TYPE_ORIENTATION -> "Orientation Recording"
            else -> "Unknown Sensortype Recording"
        }
    }


}