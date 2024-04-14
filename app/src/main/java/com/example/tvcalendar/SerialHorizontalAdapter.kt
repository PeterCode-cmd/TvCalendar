package com.example.tvcalendar

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SerialHorizontalAdapter(private val serials: List<Serial>) : RecyclerView.Adapter<SerialHorizontalAdapter.SerialViewHolder>() {
    class SerialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val posterImageView: ImageView = itemView.findViewById(R.id.ivPosterImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.horizontal_item_serial, parent, false)
        return SerialViewHolder(view)
    }

    override fun getItemCount(): Int {
        return serials.size
    }

    override fun onBindViewHolder(holder: SerialViewHolder, position: Int) {
        val serial = serials[position]
        Glide.with(holder.itemView)
            .load(serial.posterURL)
            .into(holder.posterImageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("serial_id", serial.id)
            intent.putExtra("backdropUrl", serial.backdrop)
            intent.putExtra("overview", serial.overview)
            intent.putExtra("title", serial.title)
            intent.putExtra("original_name", serial.originalName)
            intent.putExtra("user_rating", serial.userRating)
            intent.putExtra("user_rating_count", serial.userRatingCount)
            holder.itemView.context.startActivity(intent)
        }
    }
}