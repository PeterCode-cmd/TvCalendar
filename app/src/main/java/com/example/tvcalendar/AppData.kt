package com.example.tvcalendar

import java.time.LocalDate


object AppData {
    var selectedGenres: MutableList<String> = mutableListOf()
    var selectedChannels: MutableList<String> = mutableListOf()
    var selectedVotes: String = ""
    var selectedYear: String = ""
    var isWatchlist: Boolean = false
}

object ApiQueryManager{
    var url: String = ""
    var query: String = ""
}

object DateManager{
    var date: String = LocalDate.now().toString()
}