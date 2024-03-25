package com.example.tvcalendar

import EpisodeListAdapter
import android.app.DownloadManager.Request
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.json.JSONException
import kotlin.math.log

class DetailsActivity : AppCompatActivity() {

    private lateinit var ivBanner: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvOverview: TextView
    private lateinit var btnShowMore: TextView
    private lateinit var tvType: TextView
    private lateinit var tvLanguage: TextView
    private lateinit var tvTagline: TextView
    private lateinit var spinnerSeasons: Spinner
    private lateinit var tvNextEpisode: TextView
    private lateinit var tvEpisodesList: MutableList<Episode>
    private lateinit var episodesRecyclerView: RecyclerView
    private lateinit var loadingSpinner: ProgressBar
    var ytURL:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        ivBanner = findViewById(R.id.bannerImageView)
        tvTitle = findViewById(R.id.titleTextView)
        tvOverview = findViewById(R.id.descriptionTextView)
        btnShowMore = findViewById(R.id.expandButton)
        tvType = findViewById(R.id.tvType)
        tvLanguage = findViewById(R.id.tvLanguages)
        tvTagline = findViewById(R.id.tvTagline)
        spinnerSeasons = findViewById(R.id.spinner_seasons)
        tvNextEpisode = findViewById(R.id.tvNextEpisode)
        episodesRecyclerView = findViewById(R.id.episodesRecyclerView)
        loadingSpinner = findViewById(R.id.progress_loader)
        tvEpisodesList = mutableListOf()

        val youTubePlayerView = findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        val typesStringBuilder = StringBuilder()
        val networksStringBuilder = StringBuilder()

        val serialId = intent.getIntExtra("serial_id", - 1)
        val backdropURL = intent.getStringExtra("backdropUrl")
        val overview = intent.getStringExtra("overview")
        val title = intent.getStringExtra("title")

        Log.d("Details Activity", "Series ID: $serialId")

        Glide.with(this)
            .load(backdropURL)
            .into(ivBanner)

        tvTitle.text = title
        tvOverview.text = overview


        if(serialId == -1)
        {
            Toast.makeText(this, "Błędne id", Toast.LENGTH_SHORT).show()
        }
        else
        {
            loadingSpinner.visibility = View.VISIBLE

            val queue = Volley.newRequestQueue(this)

            val url = "https://api.themoviedb.org/3/tv/$serialId?append_to_response=videos&language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

            Log.d("Url request", "URL REQUEST: $url")

            val jsonObjectRequest = JsonObjectRequest(url, {response ->

                Log.d("Url request", "URL REQUEST2: $url")

                val jsonVideosObject = response.getJSONObject("videos")
                val jsonVideosArray = jsonVideosObject.getJSONArray("results")
                for (i in 0 until jsonVideosArray.length()) {
                    val result = jsonVideosArray.getJSONObject(i)
                    if (result.getString("type") == "Trailer") {
                        ytURL = result.getString("key")

                        Log.d("Details activity", "Gownozasrane: $ytURL")
                    }
                }

                youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        val videoId = ytURL
                        if(videoId.isEmpty())
                        {
                            youTubePlayerView.visibility = View.GONE
                        }
                        else{
                            youTubePlayer.cueVideo(videoId, 0f)
                            youTubePlayer.mute()
                            ivBanner.visibility = View.INVISIBLE
                            youTubePlayerView.visibility= View.VISIBLE
                        }
                    }
                })


                val jsonTagline = response.getString("tagline")
                val jsonGenresArray = response.getJSONArray("genres")
                for(i in 0 until jsonGenresArray.length())
                {
                    val genreObject = jsonGenresArray.getJSONObject(i)
                    val name = genreObject.getString("name")

                    typesStringBuilder.append(name)

                    if(i < jsonGenresArray.length() - 1)
                    {
                        typesStringBuilder.append(", ")
                    }
                }

                val networksArray = response.getJSONArray("networks")
                for (i in 0 until networksArray.length()) {

                    val networkObject = networksArray.getJSONObject(i)
                    val name = networkObject.getString("name")

                    networksStringBuilder.append(name)

                    if (i < networksArray.length() - 1)
                    {
                        networksStringBuilder.append(", ")
                    }
                }

                val seasonsArray = response.getJSONArray("seasons")

                val seasonNames = mutableListOf<String>()

                for (i in 0 until seasonsArray.length()) {

                    val seasonObject = seasonsArray.getJSONObject(i)

                    val seasonName = seasonObject.getString("name")

                    seasonNames.add(seasonName)
                }

                if (response.has("next_episode_to_air") && !response.isNull("next_episode_to_air")) {

                    val nextEpisode = response.getJSONObject("next_episode_to_air")

                    val airDate = if (nextEpisode.has("air_date")) nextEpisode.getString("air_date") else ""
                    val episodeNumber = if (nextEpisode.has("episode_number")) nextEpisode.getInt("episode_number") else -1
                    val seasonNumber = if (nextEpisode.has("season_number")) nextEpisode.getInt("season_number") else -1

                    if (airDate.isNotEmpty() && episodeNumber != -1 && seasonNumber != -1) {
                        val text = "Następny odcinek (sezon ${seasonNumber}, odcinek ${episodeNumber}) wychodzi ${airDate}"

                        tvNextEpisode.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        tvNextEpisode.text = "Brak informacji o następnym odcinku"
                    }
                } else {
                    tvNextEpisode.text = "Brak informacji o następnym odcinku"
                }

                val adapter = ArrayAdapter<String>(this@DetailsActivity, android.R.layout.simple_spinner_item, seasonNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSeasons.adapter = adapter

                if(jsonTagline.isEmpty())
                {
                    tvTagline.visibility = View.GONE
                }
                else
                {
                    tvTagline.visibility = View.VISIBLE
                    tvTagline.text = jsonTagline
                }
                tvLanguage.text = networksStringBuilder.toString()
                tvType.text = typesStringBuilder.toString()

            }, {error ->

                error.printStackTrace()
            })
            queue.add(jsonObjectRequest)
        }

        fetchVideo(serialId)

        tvOverview.post{

            val lineCount = tvOverview.lineCount

            if(lineCount >= 2)
            {
                btnShowMore.visibility = View.VISIBLE

                btnShowMore.setOnClickListener {

                    if(btnShowMore.text == getString(R.string.expand))
                    {
                        tvOverview.maxLines = Int.MAX_VALUE
                        btnShowMore.text = getString(R.string.collapse)
                    }
                    else
                    {
                        tvOverview.maxLines = 2
                        btnShowMore.text = getString(R.string.expand)
                    }
                }
            }

        }

        episodesRecyclerView.layoutManager = LinearLayoutManager(this)

        spinnerSeasons.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                episodesRecyclerView.visibility = View.GONE

                loadingSpinner.visibility = View.VISIBLE

                val selectedSeason = spinnerSeasons.selectedItem as String
                if(selectedSeason == "Miniserial")
                {
                    val seasonNumber = 1
                    getEpisodesForSeason(seasonNumber.toString(), serialId) { episodesList ->
                        val adapter = EpisodeListAdapter(episodesList)
                        episodesRecyclerView.adapter = adapter
                        episodesRecyclerView.visibility = View.VISIBLE
                        loadingSpinner.visibility = View.GONE
                    }
                }
                else {

                    val seasonNumber = selectedSeason.split(" ")[1]
                    getEpisodesForSeason(seasonNumber, serialId) { episodesList ->
                        val adapter = EpisodeListAdapter(episodesList)
                        loadingSpinner.visibility = View.GONE
                        episodesRecyclerView.visibility = View.VISIBLE
                        episodesRecyclerView.adapter = adapter
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun fetchVideo(seriedId: Int)
    {
        val queue = Volley.newRequestQueue(this)

        val url = "https://api.themoviedb.org/3/tv/$seriedId?append_to_response=videos&language=en-US&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

        Log.d("Url request", "URL REQUEST: $url")

        val jsonObjectRequest = JsonObjectRequest(url, { response ->

            Log.d("Url request", "URL REQUEST2: $url")

            val jsonVideosObject = response.getJSONObject("videos")
            val jsonVideosArray = jsonVideosObject.getJSONArray("results")
            for (i in 0 until jsonVideosArray.length()) {
                val result = jsonVideosArray.getJSONObject(i)
                if (result.getString("type") == "Trailer") {
                    ytURL = result.getString("key")

                    Log.d("Details activity", "Gownozasrane: $ytURL")
                }
            }
        },{ error ->

            error.printStackTrace()
        })

        queue.add(jsonObjectRequest)
    }

    private fun getEpisodesForSeason(seasonNumber: String, serialId: Int, onEpisodesLoaded: (List<Episode>) -> Unit) {

        loadingSpinner.visibility = View.VISIBLE

        val episodesList = mutableListOf<Episode>()

        val queue = Volley.newRequestQueue(this@DetailsActivity)

        val url = "https://api.themoviedb.org/3/tv/$serialId/season/$seasonNumber?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

        val request = JsonObjectRequest(url, { response ->
            try {
                val jsonEpisodesArray = response.getJSONArray("episodes")
                for (i in 0 until jsonEpisodesArray.length()) {
                    val episodesObject = jsonEpisodesArray.getJSONObject(i)
                    val name = episodesObject.getString("name")
                    val air_date = episodesObject.optString("air_date", "")
                    val episode_number = episodesObject.optInt("episode_number", -1)
                    val runtime = episodesObject.optInt("runtime", -1)

                    if (air_date.isNotEmpty() && episode_number != -1 && runtime != -1) {
                        val episode = Episode(name ,air_date, episode_number, runtime)
                        episodesList.add(episode)
                        loadingSpinner.visibility = View.GONE
                    } else {
                        Log.e("DetailsActivity", "Missing or invalid episode data")
                        loadingSpinner.visibility = View.GONE
                    }
                }

                onEpisodesLoaded(episodesList)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, { error ->
            error.printStackTrace()
        })

        queue.add(request)
    }



}