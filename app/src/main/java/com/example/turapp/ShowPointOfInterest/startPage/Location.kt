package com.example.turapp.ShowPointOfInterest.startPage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// BASED ON https://youtu.be/HtwDXRWjMcU

@Parcelize
data class Location (
    val title:String,
    val distance:Int
): Parcelable
