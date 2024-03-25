package com.example.tvcalendar

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView

class FiltersDiagloFragment(private val onFilterSelectedListener: OnFilterSelectedListener, private val serialsList: MutableList<Serial>, private val recyclerView: RecyclerView) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_serial_filters, null)

        val yearSpinner: Spinner = view.findViewById(R.id.yearSpinner)
        val sortSpinner: Spinner = view.findViewById(R.id.sortSpinner)
        val applyButton: Button = view.findViewById(R.id.applyButton)
        val voteSpinner: Spinner = view.findViewById(R.id.votesSpinner)
        val providersSpinner: Spinner = view.findViewById(R.id.providerSpinner)
        val genresSpinner: Spinner = view.findViewById(R.id.genresSpinner)

        val years = listOf("2003","2004","2005","2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016","2017","2018","2019","2020","2021","2022", "2023", "2024") // Tutaj możesz zamiast tego pobrać dynamicznie dostępne lata
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        val sortOptions = listOf("Najnowsze", "Ilość głosów", "Popularność", "Ocena")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        val voteCount = listOf("0", "50", "100", "200", "400", "600", "1000", "2000", "5000")
        val voteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, voteCount)
        voteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voteSpinner.adapter = voteAdapter

        val providers = listOf("Netflix", "Amazon Prime", "Disney+", "Apple TV Plus", "SkyShowtime", "Hbo Max")
        val spinnerAdapter = ProvidersSpinnerAdapter(requireContext(), providers)
        providersSpinner.adapter = spinnerAdapter

        val genres = listOf("Akcja i przygoda", "Animacja", "Komedia", "Kryminał", "Dokumentalny", "Dramat", "Sci-fi", "Western")
        val spinnerAdapter2 = ProvidersSpinnerAdapter(requireContext(), genres)
        genresSpinner.adapter = spinnerAdapter2

        applyButton.setOnClickListener {

            val selectedYear = years[yearSpinner.selectedItemPosition]
            val selectedSort = sortOptions[sortSpinner.selectedItemPosition]
            val selectedVote = voteCount[voteSpinner.selectedItemPosition]
            val selectedProviders = spinnerAdapter.getSelectedProviders()
            val selectedGenres = spinnerAdapter2.getSelectedProviders()
            val currentPage = 1
            serialsList.clear()
            recyclerView.scrollToPosition(0)
            onFilterSelectedListener.onFilterApplied(selectedYear, selectedSort, selectedVote, selectedProviders, selectedGenres, currentPage)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    interface OnFilterSelectedListener {
        fun onFilterApplied(year: String, sortCriteria: String, voteAmount: String, selectedProviders: List<String>, selectedGenres: List<String>, currentPage: Int)
    }
}
