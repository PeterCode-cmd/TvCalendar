package com.example.tvcalendar

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class EpisodeDetailsDialogFragment(private val episode: Episode):DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_episode_details, null)

        val posterImageView: ImageView = view.findViewById(R.id.dialog_posterImageView)
        val titleTextView: TextView = view.findViewById(R.id.dialog_titleTextView)
        val overviewTextView: TextView = view.findViewById(R.id.dialog_overviewTextView)

        Glide.with(requireContext()).load(episode.imageURL).into(posterImageView)
        titleTextView.text = episode.name
        overviewTextView.text = episode.overview

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

    }
}