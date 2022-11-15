package com.example.turapp.repository.trackingDb.entities

data class MyPointWeek (
    val week: Int,
    val earliest: String,
    val latest: String,
    val count: Int,
)
