package com.example.tvcalendar

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog.TAG
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.tvcalendar.R
import com.example.tvcalendar.Serial
import org.json.JSONObject


class SerialDetailsDialogFragment(private val serial: Serial) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_serial_details, null)

        val posterImageView: ImageView = view.findViewById(R.id.dialog_posterImageView)
        val titleTextView: TextView = view.findViewById(R.id.dialog_titleTextView)
        val releaseDateTextView: TextView = view.findViewById(R.id.dialog_releaseDateTextView)
        val overviewTextView: TextView = view.findViewById(R.id.dialog_overviewTextView)

        if(serial.backdrop.isEmpty()){
            Glide.with(requireContext()).load(serial.posterURL).into(posterImageView)
        }
        else
        {
            Glide.with(requireContext()).load(serial.backdrop).into(posterImageView)
        }

        titleTextView.text = serial.title
        releaseDateTextView.text = "Premiera: ${serial.releaseDate}"
        overviewTextView.text = serial.overview

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

}