package com.example.tvcalendar

data class Serial(

    val id: Int,
    val title: String,
    val originalName: String,
    val releaseDate: String,
    val overview: String,
    val posterURL: String,
    val userRating: Double,
    val userRatingCount: Int,
    val backdrop: String,
    var isInWatchlist: Boolean

)
