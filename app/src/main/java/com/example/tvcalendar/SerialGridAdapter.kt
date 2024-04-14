package com.example.tvcalendar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyLog
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class SerialGridAdapter(private val serials: List<Serial>) : RecyclerView.Adapter<SerialGridAdapter.SerialViewHolder>() {

    private var db = Firebase.firestore
    private lateinit var sharedPreferences: SharedPreferences
    class SerialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImageView: ImageView = itemView.findViewById(R.id.ivPosterImage)
        val tvReleaseDate: TextView = itemView.findViewById(R.id.tvReleaseDate)
        val watchlistButton: ImageView = itemView.findViewById(R.id.watchlistButton)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_loader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_serial, parent, false)
        sharedPreferences = parent.context.getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)
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

        holder.tvReleaseDate.text = serial.releaseDate

        holder.posterImageView.setOnClickListener {
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


        sharedPreferences = holder.itemView.context.getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)

        val isInWatchlist = sharedPreferences.getBoolean(serial.id.toString(), false)

        if (isInWatchlist) {
            holder.watchlistButton.setImageResource(R.drawable.oczkofull)
        } else {
            holder.watchlistButton.setImageResource(R.drawable.watchlist)
        }

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        holder.watchlistButton.setOnClickListener {
            holder.watchlistButton.visibility = View.INVISIBLE
            holder.progressBar.visibility = View.VISIBLE

            val serialMap = hashMapOf(
                "serialId" to serial.id,
                "timestamp" to FieldValue.serverTimestamp()
            )

            val collectionRef = db.collection("users").document(userId).collection("watchList")
            val serialDocRef = collectionRef.document(serial.id.toString())

            serialDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        serialDocRef.delete()
                            .addOnSuccessListener {
                                holder.watchlistButton.visibility = View.INVISIBLE
                                holder.progressBar.visibility = View.VISIBLE

                                Log.d(VolleyLog.TAG, "Serial removed successfully")
                                // Zaktualizuj interfejs użytkownika
                                holder.watchlistButton.setImageResource(R.drawable.watchlist) // Ustaw białą ikonkę
                                serial.isInWatchlist = false // Zaktualizuj wartość isInWatchlist
                                // Zapisz stan w SharedPreferences
                                sharedPreferences.edit().putBoolean(serial.id.toString(), false).apply()

                                holder.progressBar.visibility = View.GONE
                                holder.watchlistButton.visibility = View.VISIBLE
                            }
                            .addOnFailureListener { e ->
                                Log.w(VolleyLog.TAG, "Error removing serial", e)
                            }
                    } else {
                        serialDocRef.set(serialMap)
                            .addOnSuccessListener {
                                holder.watchlistButton.visibility = View.INVISIBLE
                                holder.progressBar.visibility = View.VISIBLE

                                Log.d(VolleyLog.TAG, "Serial added successfully")
                                // Zaktualizuj interfejs użytkownika
                                holder.watchlistButton.setImageResource(R.drawable.oczkofull) // Ustaw żółtą ikonkę
                                serial.isInWatchlist = true // Zaktualizuj wartość isInWatchlist
                                // Zapisz stan w SharedPreferences
                                sharedPreferences.edit().putBoolean(serial.id.toString(), true).apply()

                                holder.progressBar.visibility = View.GONE
                                holder.watchlistButton.visibility = View.VISIBLE
                            }
                            .addOnFailureListener { e ->
                                Log.w(VolleyLog.TAG, "Error adding serial", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(VolleyLog.TAG, "Error checking serial existence", exception)
                }

        }
    }
}