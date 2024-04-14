package com.example.tvcalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.tvcalendar.databinding.LayoutModalBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomBottomSheetDialogFragment(private val serialsList: MutableList<Serial>,private val recyclerView: RecyclerView) : BottomSheetDialogFragment() {
    private lateinit var binding: LayoutModalBottomSheetBinding

    interface OnApplyButtonClickListener {
        fun onApplyButtonClick(genres: List<String>, channels: List<String>, votes: String, year: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LayoutModalBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Przywróć stan, jeśli istnieje
        savedInstanceState?.let { savedState ->
            AppData.selectedGenres = savedState.getStringArrayList("selectedGenres")?.toMutableList() ?: mutableListOf()
            AppData.selectedChannels = savedState.getStringArrayList("selectedChannels")?.toMutableList() ?: mutableListOf()
            AppData.selectedVotes = savedState.getString("selectedVotes") ?: ""
            AppData.selectedYear = savedState.getString("selectedYear") ?: ""
        }

        val yearsArray = resources.getStringArray(R.array.years_array)
        val yearsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, yearsArray)
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.yearsSpinner.adapter = yearsAdapter

        val votesArray = resources.getStringArray(R.array.votes_array)
        val votesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, votesArray)
        votesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.votesSpinner.adapter = votesAdapter

        AppData.selectedGenres.forEach { genre ->
            binding.gatunki.findViewWithTag<AppCompatCheckBox>(genre)?.isChecked = true
        }
        AppData.selectedChannels.forEach { channel ->
            binding.kanaly.findViewWithTag<AppCompatCheckBox>(channel)?.isChecked = true
        }

        val selectedYearPosition = yearsAdapter.getPosition(AppData.selectedYear)
        binding.yearsSpinner.setSelection(selectedYearPosition)

        val selectedVotesPosition = votesAdapter.getPosition(AppData.selectedVotes)
        binding.votesSpinner.setSelection(selectedVotesPosition)

        binding.btnApply.setOnClickListener {
            // Pobranie wyborów użytkownika
            AppData.selectedGenres.clear()
            AppData.selectedChannels.clear()
            for (i in 0 until binding.gatunki.childCount) {
                val checkBox = binding.gatunki.getChildAt(i) as? AppCompatCheckBox
                if (checkBox?.isChecked == true) {
                    AppData.selectedGenres.add(checkBox.text.toString())
                }
            }
            for (i in 0 until binding.kanaly.childCount) {
                val checkBox = binding.kanaly.getChildAt(i) as? AppCompatCheckBox
                if (checkBox?.isChecked == true) {
                    AppData.selectedChannels.add(checkBox.text.toString())
                }
            }
            AppData.selectedVotes = binding.votesSpinner.selectedItem.toString()
            AppData.selectedYear = binding.yearsSpinner.selectedItem.toString()


            serialsList.clear()
            recyclerView.scrollToPosition(0)
            // Przekazanie wyborów do MainActivity
            (activity as? OnApplyButtonClickListener)?.onApplyButtonClick(AppData.selectedGenres, AppData.selectedChannels, AppData.selectedVotes, AppData.selectedYear)

            // Zamknięcie okna modalnego
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Zapisz stan wybranych filtrów
        outState.putStringArrayList("selectedGenres", ArrayList(AppData.selectedGenres))
        outState.putStringArrayList("selectedChannels", ArrayList(AppData.selectedChannels))
        outState.putString("selectedVotes", AppData.selectedVotes)
        outState.putString("selectedYear", AppData.selectedYear)
    }
}