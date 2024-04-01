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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyLog.TAG
import com.bumptech.glide.Glide
import com.example.tvcalendar.DetailsActivity
import com.example.tvcalendar.R
import com.example.tvcalendar.Serial
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore


class SerialAdapter(private val serials: List<Serial>) : RecyclerView.Adapter<SerialAdapter.SerialViewHolder>(), View.OnClickListener {

    private var db = Firebase.firestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_serial, parent, false)
        sharedPreferences = parent.context.getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)
        return SerialViewHolder(view)
    }
    override fun onBindViewHolder(holder: SerialViewHolder, position: Int) {

        val serial = serials[position]

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("serial_id", serial.id)
            intent.putExtra("backdropUrl", serial.backdrop)
            intent.putExtra("overview", serial.overview)
            intent.putExtra("title", serial.title)
            intent.putExtra("original_name", serial.originalName)
            holder.itemView.context.startActivity(intent)
        }

        /*if(selectedDate.toString() == serial.releaseDate) {
            holder.posterImageView.visibility = View.GONE
            Log.d("selectedDate", selectedDate.toString())
        } else {
            holder.posterImageView.visibility = View.VISIBLE
            Log.d("selectedDate", selectedDate.toString())
        }*/

        if (!serial.title.isNullOrEmpty()) {
            holder.titleTextView.text = serial.title
        } else {
            holder.titleTextView.text = "Brak tytułu"
        }

        if (!serial.releaseDate.isNullOrEmpty()) {
            holder.releaseDateTextView.text = serial.releaseDate
        } else {
            holder.releaseDateTextView.text = "Brak daty premiery"
        }

        sharedPreferences = holder.itemView.context.getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)

        val isInWatchlist = sharedPreferences.getBoolean(serial.id.toString(), false)

        if (isInWatchlist) {
            holder.imdbIcon.setImageResource(R.drawable.oczkozolte)
        } else {
            holder.imdbIcon.setImageResource(R.drawable.oczkoxd)
        }

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        holder.imdbIcon.setOnClickListener {
            holder.imdbIcon.visibility = View.INVISIBLE
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
                                holder.imdbIcon.visibility = View.INVISIBLE
                                holder.progressBar.visibility = View.VISIBLE

                                Log.d(TAG, "Serial removed successfully")
                                // Zaktualizuj interfejs użytkownika
                                holder.imdbIcon.setImageResource(R.drawable.oczkoxd) // Ustaw białą ikonkę
                                serial.isInWatchlist = false // Zaktualizuj wartość isInWatchlist
                                // Zapisz stan w SharedPreferences
                                sharedPreferences.edit().putBoolean(serial.id.toString(), false).apply()

                                holder.progressBar.visibility = View.GONE
                                holder.imdbIcon.visibility = View.VISIBLE
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error removing serial", e)
                            }
                    } else {
                        serialDocRef.set(serialMap)
                            .addOnSuccessListener {
                                holder.imdbIcon.visibility = View.INVISIBLE
                                holder.progressBar.visibility = View.VISIBLE

                                Log.d(TAG, "Serial added successfully")
                                // Zaktualizuj interfejs użytkownika
                                holder.imdbIcon.setImageResource(R.drawable.oczkozolte) // Ustaw żółtą ikonkę
                                serial.isInWatchlist = true // Zaktualizuj wartość isInWatchlist
                                // Zapisz stan w SharedPreferences
                                sharedPreferences.edit().putBoolean(serial.id.toString(), true).apply()

                                holder.progressBar.visibility = View.GONE
                                holder.imdbIcon.visibility = View.VISIBLE
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding serial", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error checking serial existence", exception)
                }

        }

        val formattedRating = String.format("%.1f", serial.userRating)


        /*val ratingText = "Ocena: $formattedRating/${serial.userRatingCount}"
        val spannableString = SpannableString(ratingText)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)*/

        holder.userRating.text = formattedRating
        holder.userCount.text = serial.userRatingCount.toString() + " ocen"

        Glide.with(holder.itemView).load(serial.posterURL).into(holder.posterImageView)

       /* holder.btnSeeMore.setOnClickListener{
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("serial_id", serial.id)
            intent.putExtra("backdropUrl", serial.backdrop)
            intent.putExtra("overview", serial.overview)
            intent.putExtra("title", serial.title)
            holder.itemView.context.startActivity(intent)
        }*/

        /*holder.posterImageView.setOnClickListener {
            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            val dialogFragment = SerialDetailsDialogFragment(serial)
            dialogFragment.show(fragmentManager, "SerialDetailsDialog")
        }*/
    }

    override fun getItemCount(): Int {
        return serials.size
    }

    class SerialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val btnSeeMore: TextView = itemView.findViewById(R.id.seeMoreButton)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_loader)
        val imdbIcon: ImageView = itemView.findViewById(R.id.btnWatchlist)
        val userRating: TextView = itemView.findViewById(R.id.tvUserRating)
        val userCount: TextView = itemView.findViewById(R.id.tvUserCount)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
        val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }



    override fun onClick(v: View?) {

    }
}