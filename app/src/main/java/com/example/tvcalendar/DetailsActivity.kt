    package com.example.tvcalendar
    
    import EpisodeListAdapter
    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
    import com.android.volley.VolleyLog
    import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
    import com.google.firebase.Firebase
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.firestore
    import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.json.JSONException
import org.json.JSONObject

    class DetailsActivity : AppCompatActivity() {
    
        //private lateinit var imageContainer : LinearLayout
        private var db = Firebase.firestore
        private lateinit var watchlistButton: ImageView
        private lateinit var progressBar: ProgressBar
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var tvLastEpisode: TextView
        private lateinit var ivPosterImageView: ImageView
        private lateinit var ivPosterSeeMore: ImageView
        private lateinit var tvEpisodeName : TextView
        private lateinit var tvEpisodeRuntime : TextView
        private lateinit var tvEpisodeNumber : TextView
        private lateinit var tvEpisodeAirDate : TextView
        private lateinit var latestLayout: LinearLayout
        private lateinit var bannerLayout : RelativeLayout
        private lateinit var ivPlayButton: ImageView
        private lateinit var tvuserRatingCount: TextView
        private lateinit var tvuserRating: TextView
        private lateinit var tvReccomendations: TextView
        private lateinit var serialsListAdapter: SerialHorizontalAdapter
        private lateinit var serialsListDetails: MutableList<Serial>
        private lateinit var recyclerViewDetails: RecyclerView
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
    
            //imageContainer = findViewById(R.id.image_container)
            progressBar = findViewById(R.id.progress_loader2)
            sharedPreferences = getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)
            watchlistButton = findViewById(R.id.watchlistButton)
            tvLastEpisode = findViewById(R.id.tvLastEpisode)
            ivPosterImageView = findViewById(R.id.posterImageView)
            ivPosterSeeMore = findViewById(R.id.posterSeeMore)
            tvEpisodeName = findViewById(R.id.tvEpisodeName)
            tvEpisodeRuntime = findViewById(R.id.tvEpisodeRuntime)
            tvEpisodeNumber = findViewById(R.id.tvEpisodeNumber)
            tvEpisodeAirDate = findViewById(R.id.tvEpisodeAirDate)
            latestLayout = findViewById(R.id.latestLayout)
            bannerLayout = findViewById(R.id.bannerLayout)
            ivPlayButton = findViewById(R.id.ivPlayButton)
            tvuserRatingCount = findViewById(R.id.tvUserCount)
            tvuserRating = findViewById(R.id.tvUserRating)
            tvReccomendations = findViewById(R.id.tvPolecane)
            recyclerViewDetails = findViewById(R.id.recyclerViewDetails)
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
    
            serialsListDetails = mutableListOf()
            serialsListAdapter = SerialHorizontalAdapter(serialsListDetails)
            recyclerViewDetails.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerViewDetails.adapter = serialsListAdapter
    
            val youTubePlayerView = findViewById<YouTubePlayerView>(R.id.youtube_player_view)
            lifecycle.addObserver(youTubePlayerView)
    
            val typesStringBuilder = StringBuilder()
            val networksStringBuilder = StringBuilder()
    
            val serialId = intent.getIntExtra("serial_id", - 1)
            val backdropURL = intent.getStringExtra("backdropUrl")
            val overview = intent.getStringExtra("overview")
            val title = intent.getStringExtra("title")
            val originalName = intent.getStringExtra("original_name")
            val userRating = intent.getDoubleExtra("user_rating", 0.0)
            val userRatingCount = intent.getIntExtra("user_rating_count", 0)
    
            val formattedRating = String.format("%.1f", userRating)
    
            tvuserRatingCount.text = userRatingCount.toString() + " ocen"
            tvuserRating.text = formattedRating
    
            fetchRecommendations(serialId)
    
            tvReccomendations.text = "Jeśli lubisz $title :"
    
            Log.d("Details Activity", "Series ID: $serialId")
    
            Glide.with(this)
                .load(backdropURL)
                .into(ivBanner)
    
            if(title == originalName)
            {
                tvTitle.text = title
            }else{
                tvTitle.text = title + " / " + originalName
            }
            tvOverview.text = overview

            ivPlayButton.setOnClickListener {
                val intent = Intent(this, VideoActivity::class.java)
                intent.putExtra("videoId", ytURL)
                startActivity(intent)
            }
    
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
    
                            Log.d("DetailsACTUS", "msg: $ytURL")
                        }
                    }
    
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
    
                        /*val imageView = ImageView(this)
    
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(8, 0, 8, 0)
                        imageView.layoutParams = layoutParams
    
                        Glide.with(this)
                            .load("https://image.tmdb.org/t/p/w500" + logo_path).override(150,150)
                            .into(imageView)
    
                        imageContainer.addView(imageView)*/
    
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

                    if (response.has("last_episode_to_air") && !response.isNull("last_episode_to_air")) {

                        val lastEpisode = response.getJSONObject("last_episode_to_air")

                        val airDateLast = if (lastEpisode.has("air_date")) lastEpisode.getString("air_date") else ""
                        val episodeNumberLast = if (lastEpisode.has("episode_number")) lastEpisode.getInt("episode_number") else -1
                        val seasonNumberLast = if (lastEpisode.has("season_number")) lastEpisode.getInt("season_number") else -1
                        val nameLast = if (lastEpisode.has("name")) lastEpisode.getString("name") else ""
                        val idLast = if (lastEpisode.has("id")) lastEpisode.getInt("id") else -1
                        val overviewLast = if (lastEpisode.has("overview")) lastEpisode.getString("overview") else ""
                        val runtimeLast = if(lastEpisode.has("runtime") && !lastEpisode.isNull("runtime")) lastEpisode.getInt("runtime") else tvEpisodeRuntime.visibility = View.GONE
                        val stillPathLast = if(lastEpisode.has("still_path")) lastEpisode.getString("still_path") else ""
                        val stillPathURL = "https://image.tmdb.org/t/p/w500" + stillPathLast

                        if(stillPathLast.isEmpty() || stillPathLast == "null")
                        {
                            Glide.with(this@DetailsActivity)
                                .load(R.drawable.noimage)
                                .into(ivPosterImageView)
                        }else{
                            Glide.with(this@DetailsActivity)
                                .load("https://image.tmdb.org/t/p/w500" + stillPathLast)
                                .into(ivPosterImageView)
                        }

                        val episode = Episode(nameLast, airDateLast, episodeNumberLast, runtimeLast.toString(), stillPathURL, overviewLast )

                        ivPosterSeeMore.setOnClickListener{

                            val dialog = EpisodeDetailsDialogFragment(episode)
                            dialog.show(supportFragmentManager, "EpisodeDetailsDialogFragment")

                        }

                        latestLayout.visibility = View.VISIBLE

                        Glide.with(this@DetailsActivity)
                            .load("https://image.tmdb.org/t/p/w500" + stillPathLast)
                            .into(ivPosterImageView)

                        tvEpisodeName.text = nameLast
                        tvEpisodeAirDate.text = airDateLast
                        tvEpisodeNumber.text = "sezon ${seasonNumberLast}, odcinek ${episodeNumberLast}"
                        tvEpisodeRuntime.text = runtimeLast.toString() +" min"

                        if(episode.overview.isEmpty())
                        {
                            ivPosterSeeMore.visibility = View.GONE
                        }
                        else
                        {
                            ivPosterSeeMore.visibility = View.VISIBLE
                        }

                    } else {
                        latestLayout.visibility = View.GONE
                        tvLastEpisode.visibility = View.GONE
                    }
    
                    val adapter = ArrayAdapter(this@DetailsActivity, android.R.layout.simple_spinner_item, seasonNames)
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

            sharedPreferences = getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)

            val isInWatchlist = sharedPreferences.getBoolean(serialId.toString(), false)

            if (isInWatchlist) {
                watchlistButton.setImageResource(R.drawable.oczkofull)
            } else {
                watchlistButton.setImageResource(R.drawable.watchlist)
            }

            val userId = FirebaseAuth.getInstance().currentUser!!.uid

            watchlistButton.setOnClickListener {
                watchlistButton.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE

                val serialMap = hashMapOf(
                    "serialId" to serialId,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                val collectionRef = db.collection("users").document(userId).collection("watchList")
                val serialDocRef = collectionRef.document(serialId.toString())

                serialDocRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            serialDocRef.delete()
                                .addOnSuccessListener {
                                    watchlistButton.visibility = View.INVISIBLE
                                    progressBar.visibility = View.VISIBLE

                                    Log.d(VolleyLog.TAG, "Serial removed successfully")
                                    // Zaktualizuj interfejs użytkownika
                                    watchlistButton.setImageResource(R.drawable.watchlist) // Ustaw białą ikonkę
                                    //serial.isInWatchlist = false // Zaktualizuj wartość isInWatchlist
                                    // Zapisz stan w SharedPreferences
                                    sharedPreferences.edit().putBoolean(serialId.toString(), false).apply()

                                    progressBar.visibility = View.GONE
                                    watchlistButton.visibility = View.VISIBLE
                                }
                                .addOnFailureListener { e ->
                                    Log.w(VolleyLog.TAG, "Error removing serial", e)
                                }
                        } else {
                            serialDocRef.set(serialMap)
                                .addOnSuccessListener {
                                    watchlistButton.visibility = View.INVISIBLE
                                    progressBar.visibility = View.VISIBLE

                                    Log.d(VolleyLog.TAG, "Serial added successfully")
                                    // Zaktualizuj interfejs użytkownika
                                    watchlistButton.setImageResource(R.drawable.oczkofull) // Ustaw żółtą ikonkę
                                    //serial.isInWatchlist = true // Zaktualizuj wartość isInWatchlist
                                    // Zapisz stan w SharedPreferences
                                    sharedPreferences.edit().putBoolean(serialId.toString(), true).apply()

                                    progressBar.visibility = View.GONE
                                    watchlistButton.visibility = View.VISIBLE
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
    
                        Log.d("Details activity", "link: $ytURL")
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
                        val imageURL = "https://image.tmdb.org/t/p/w500" + episodesObject.optString("still_path", "")
                        val overview = episodesObject.optString("overview", "")
    
                        if (air_date.isNotEmpty() && episode_number != -1 && runtime != -1) {
                            val episode = Episode(name ,air_date, episode_number, runtime.toString(), imageURL, overview)
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
    
        private fun fetchRecommendations(serialId: Int){
    
            val url = "https://api.themoviedb.org/3/tv/$serialId/recommendations?language=pl-PL&page=1&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"
    
            val request = JsonObjectRequest(
                Request.Method.GET ,url, null,
                { response ->
                    val serials = parseResponse(response)
    
                    serialsListDetails.clear()
                    serialsListDetails.addAll(serials)
                    serialsListAdapter.notifyDataSetChanged()
    
                },
                { error ->
                    error.printStackTrace()
                })
    
            Volley.newRequestQueue(this).add(request)
    
        }
    
        private fun parseResponse(response: JSONObject): List<Serial> {
            val serials = mutableListOf<Serial>()
            val results = response.getJSONArray("results")
            for (i in 0 until results.length()) {
                val serialJson = results.getJSONObject(i)
                val posterPath = serialJson.getString("poster_path")
                val overview = serialJson.getString("overview")
                val userRating = serialJson.getDouble("vote_average")
                val title = serialJson.getString("name")
                val releaseDate = serialJson.getString("first_air_date")
                val userRatingCount = serialJson.getInt("vote_count")
                val posterUrl = "https://image.tmdb.org/t/p/w500" + posterPath
                val id = serialJson.getInt("id")
                val backdrop = serialJson.getString("backdrop_path")
                val backdropUrl = "https://image.tmdb.org/t/p/w500" + backdrop
                val originalName = serialJson.getString("original_name")
    
                    if(posterPath != "null")
                    {
                        serials.add(Serial( id ,title, originalName, releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))
                    }
            }
            return serials
        }
    }