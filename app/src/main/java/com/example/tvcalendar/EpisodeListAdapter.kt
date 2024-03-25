import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tvcalendar.Episode
import com.example.tvcalendar.R

class EpisodeListAdapter(private val episodes: List<Episode>) : RecyclerView.Adapter<EpisodeListAdapter.EpisodeViewHolder>() {

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEpisodeName: TextView = itemView.findViewById(R.id.tvEpisodeName)
        val tvEpisodeRuntime: TextView = itemView.findViewById(R.id.tvEpisodeRuntime)
        val tvEpisodeNumber: TextView = itemView.findViewById(R.id.tvEpisodeNumber)
        val tvEpisodeAirDate: TextView = itemView.findViewById(R.id.tvEpisodeAirDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_item, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]

        holder.tvEpisodeName.text = episode.name
        holder.tvEpisodeRuntime.text = "Długość odcinka: ${episode.runtime} minut(y)"
        holder.tvEpisodeNumber.text = "Odcinek: ${episode.episode_number}"
        holder.tvEpisodeAirDate.text = "Premiera: ${episode.air_data}"
    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}
