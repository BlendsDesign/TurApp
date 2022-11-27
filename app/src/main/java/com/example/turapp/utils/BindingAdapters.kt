package com.example.turapp.utils

import android.app.Application
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.turapp.R
import com.example.turapp.repository.trackingDb.entities.MyPoint
import com.example.turapp.repository.trackingDb.entities.TYPE_POI
import com.example.turapp.repository.trackingDb.entities.TYPE_SNAPSHOT
import com.example.turapp.repository.trackingDb.entities.TYPE_TRACKING
import com.example.turapp.utils.RecyclerViewAdapters.MyPointListAdapter
import kotlin.coroutines.coroutineContext

@BindingAdapter("loadImage")
fun bindImage(imageView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        Glide.with(imageView.context).load(imageUrl).apply(
            RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_baseline_broken_image)
        ).into(imageView)
    }
}

@BindingAdapter("loadListImage")
fun bindListImage(imageView: ImageView, imageString: String?) {
    if (imageString == null) {
        imageView.visibility = View.GONE
    } else {
        val imageUri = Uri.parse(imageString)
        imageUri?.let {
            Glide.with(imageView.context).load(imageUri).apply(
                RequestOptions()
                    .error(R.drawable.ic_baseline_broken_image)
            ).into(imageView).view.apply {
            }
        }
    }
}

@BindingAdapter("loadUriImage")
fun bindUriImage(imageView: ImageView, imageUrl: Uri?) {
    if (imageUrl == null) {
        imageView.visibility = View.GONE
    }
    imageUrl?.let {
        Glide.with(imageView.context).load(imageUrl).apply(
            RequestOptions()
                .error(R.drawable.ic_baseline_broken_image)
        ).into(imageView).view.apply {
        }
    }
}

@BindingAdapter("selfieShowImage")
fun bindSelfieUriImage(imageView: ImageView, imageUrl: Uri?) {
    if (imageUrl == null) {
        imageView.visibility = View.GONE
    }
    imageUrl?.let {
        Glide.with(imageView.context).load(imageUrl).apply(
            RequestOptions()
                .error(R.drawable.ic_baseline_broken_image)
        ).into(imageView).view.apply {
            visibility = View.VISIBLE
        }

    }
}

@BindingAdapter("listShowImage")
fun bindListUriImage(imageView: ImageView, imageString: String?) {
    if (imageString == null) {
        imageView.visibility = View.GONE
    }
    imageString?.let {
        val imageUri = Uri.parse(it)
        Glide.with(imageView.context).load(imageUri).apply(
            RequestOptions()
                .error(R.drawable.ic_baseline_broken_image)
        ).into(imageView).view.apply {
        }
    }
    imageView.visibility = View.VISIBLE
}

@BindingAdapter("setListAdapterIcon")
fun bindImageView(view: ImageView, myPoint: MyPoint?) {
    myPoint?.let {
        val noImage = when (it.type) {
            TYPE_POI -> R.drawable.ic_marker_blue
            TYPE_TRACKING -> R.drawable.ic_run_man
            TYPE_SNAPSHOT -> R.drawable.ic_camera
            else -> R.drawable.ic_baseline_broken_image
        }
        view.setImageResource(noImage)
    }
}