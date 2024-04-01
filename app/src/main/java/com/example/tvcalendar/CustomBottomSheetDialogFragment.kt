package com.example.tvcalendar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var listener: FilterSelectionListener? = null

    private val genres = listOf("Akcja i przygoda", "Animacja", "Komedia", "Kryminał", "Dokumentalny", "Dramat", "Sci-fi", "Western")
    private val channels = listOf("Netflix", "Amazon Prime", "Disney+", "Apple TV Plus", "SkyShowtime", "Hbo Max")
    private val votes = listOf("50", "100", "150", "250", "350", "500", "1000", "2000", "5000")
    private val years = listOf("2003","2004","2005","2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016","2017","2018","2019","2020","2021","2022", "2023", "2024")

    private val selectedGenres = mutableListOf<String>()
    private val selectedChannels = mutableListOf<String>()
    private var selectedVotes = ""
    private var selectedYear = ""

    interface FilterSelectionListener {
        fun onFilterSelected(genres: List<String>, channels: List<String>, votes: String, year: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as FilterSelectionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement FilterSelectionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_modal_bottom_sheet, container, false)

        setupFilterSection(view.findViewById(R.id.gatunki), genres, selectedGenres)
        setupFilterSection(view.findViewById(R.id.kanaly), channels, selectedChannels)
        setupSingleChoiceFilterSection(view.findViewById(R.id.glosy), votes) { selectedVotes = it }
        setupSingleChoiceFilterSection(view.findViewById(R.id.lata), years) { selectedYear = it }

        view.findViewById<Button>(R.id.btnApply).setOnClickListener {
            listener?.onFilterSelected(selectedGenres, selectedChannels, selectedVotes, selectedYear)
            dismiss()
        }

        return view
    }

    private fun setupFilterSection(layout: LinearLayout, items: List<String>, selectedItems: MutableList<String>) {
        items.forEach { item ->
            val textView = createSelectableTextView(item)
            textView.setOnClickListener {
                it.isSelected = !it.isSelected
                if (it.isSelected) {
                    selectedItems.add(item)
                } else {
                    selectedItems.remove(item)
                }
            }
            layout.addView(textView)
        }
    }

    private fun setupSingleChoiceFilterSection(layout: LinearLayout, items: List<String>, onItemSelected: (String) -> Unit) {
        items.forEach { item ->
            val textView = createSelectableTextView(item)
            textView.setOnClickListener { view ->
                layout.children.forEach { it.isSelected = false }
                view.isSelected = true
                onItemSelected(item)
            }
            layout.addView(textView)
        }
    }

    private fun createSelectableTextView(text: String): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(text)
            setTextSize(16f)
            setPadding(8, 8, 8, 8)
            background = ContextCompat.getDrawable(context, R.drawable.noimage)
            isClickable = true
            isFocusable = true
        }
    }

    // Metoda do ustawienia listenera z aktywności/fragmentu
    fun setFilterSelectionListener(listener: FilterSelectionListener) {
        this.listener = listener
    }
}
