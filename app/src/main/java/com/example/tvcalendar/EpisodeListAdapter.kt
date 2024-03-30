import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tvcalendar.Episode
import com.example.tvcalendar.EpisodeDetailsDialogFragment
import com.example.tvcalendar.R

class EpisodeListAdapter(private val episodes: List<Episode>) : RecyclerView.Adapter<EpisodeListAdapter.EpisodeViewHolder>() {

    // Create a ViewHolder class
    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEpisodeName: TextView = itemView.findViewById(R.id.tvEpisodeName)
        val tvEpisodeRuntime: TextView = itemView.findViewById(R.id.tvEpisodeRuntime)
        val tvEpisodeNumber: TextView = itemView.findViewById(R.id.tvEpisodeNumber)
        val tvEpisodeAirDate: TextView = itemView.findViewById(R.id.tvEpisodeAirDate)
        val ivPosterSeeMore: ImageView = itemView.findViewById(R.id.posterSeeMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_item, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]

        holder.tvEpisodeName.text = episode.name
        holder.tvEpisodeRuntime.text = "${episode.runtime} min"
        holder.tvEpisodeNumber.text = "${episode.episode_number} odcinek"
        holder.tvEpisodeAirDate.text = "${episode.air_data}"


        // Load the image using Glide
        if (episode.imageURL.length > 35) {
            Glide.with(holder.itemView)
                .load(episode.imageURL)
                .into(holder.itemView.findViewById(R.id.posterImageView))
            holder.ivPosterSeeMore.visibility = View.VISIBLE
        }
        else{
            Glide.with(holder.itemView)
                .load(R.drawable.noimage)
                .into(holder.itemView.findViewById(R.id.posterImageView))
            holder.ivPosterSeeMore.visibility = View.GONE
        }

        if(episode.overview.isEmpty())
        {
            holder.ivPosterSeeMore.visibility  = View.GONE
        }

        holder.ivPosterSeeMore.setOnClickListener {
            val dialog = EpisodeDetailsDialogFragment(episode)
            dialog.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "dialog")
        }

    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}
