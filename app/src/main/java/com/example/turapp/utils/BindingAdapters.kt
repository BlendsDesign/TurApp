package com.example.turapp.utils

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.turapp.R

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

@BindingAdapter("loadUriImage")
fun bindUriImage(imageView: ImageView, imageUrl: Uri?) {
    if (imageUrl == null) {
        imageView.visibility = View.GONE
    } else {
        imageView.visibility = View.VISIBLE
    }
    imageUrl?.let {
        Glide.with(imageView.context).load(imageUrl).apply(
            RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_baseline_broken_image)
        ).into(imageView)
    }
}