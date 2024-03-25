package com.example.tvcalendar

import SerialAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAmount


class MainActivity : AppCompatActivity(), FiltersDiagloFragment.OnFilterSelectedListener {


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mostPopularSeriesButton: Button
    private lateinit var adapter: SerialAdapter
    private lateinit var serialsList: MutableList<Serial>
    private lateinit var datePicker: DatePicker
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvSectionInfo: TextView
    private lateinit var btnNextDayButton: Button
    private lateinit var btnPrevDayButton: Button
    private lateinit var calendarLayout: LinearLayout
    private lateinit var btnPremiery: Button
    private lateinit var btnMostPopularThisWeek: Button
    private lateinit var btnMostPopularThisDay: Button
    private var currentDate: LocalDate = LocalDate.now()
    private var isDatePickerVisible = false
    private lateinit var layoutMain: LinearLayout
    private lateinit var btnNoweOdcinki: Button
    private lateinit var btnFilterButton: Button
    private lateinit var etSearch: EditText
    private lateinit var btnFiltry: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnZaloguj: Button
    private lateinit var btnWatchlist: Button
    private lateinit var loadingSpinner: ProgressBar
    private var currentPage = 1
    private var currentCategory = 0
    private var isPremiery = 1
    private var isLoading = false
    private val PAGE_SIZE = 10
    private var isLastPage = false
    private var selGenre: List<String> = listOf()
    private var selProvider: List<String> = listOf()
    private var selYear: String = ""
    private var selVote: String = ""
    private var selSort: String = ""
    private var serialId: String = ""
    private var db = Firebase.firestore
    private val trendingTVDay = "https://api.themoviedb.org/3/trending/tv/day?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&page="
    private val trendingTVWeek = "https://api.themoviedb.org/3/trending/tv/week?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&page="
    private val trendingTV24 = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date_year=2024&include_adult=false&language=pl-PL&sort_by=vote_count.desc&with_poster=true&page="


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        datePicker = findViewById(R.id.datePicker)
        layoutMain = findViewById(R.id.layoutMain)
        btnPremiery = findViewById(R.id.btnPremiery)
        tvSectionInfo = findViewById(R.id.tvSectionInfo)
        drawerLayout = findViewById(R.id.drawer_layout)
        mostPopularSeriesButton = findViewById(R.id.most_popular_series_button)
        btnNextDayButton = findViewById(R.id.nextDayButton)
        btnPrevDayButton = findViewById(R.id.previousDayButton)
        tvCurrentDate = findViewById(R.id.dateTextView)
        calendarLayout = findViewById(R.id.dateSelectionLayout)
        btnMostPopularThisWeek = findViewById(R.id.most_popular_this_week)
        btnMostPopularThisDay = findViewById(R.id.most_popular_this_day)
        btnNoweOdcinki = findViewById(R.id.btnNoweOdcinki)
        btnFilterButton = findViewById(R.id.btnFilterButton)
        etSearch = findViewById(R.id.etSearch)
        btnFiltry = findViewById(R.id.btnFiltry)
        btnZaloguj = findViewById(R.id.btnZaloguj)
        btnWatchlist = findViewById(R.id.btnWatchlist)
        loadingSpinner = findViewById(R.id.progress_loader)

        serialsList = mutableListOf()
        adapter = SerialAdapter(serialsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        btnPrevDayButton.visibility = View.GONE
        btnNextDayButton.visibility = View.GONE
        calendarLayout.visibility = View.GONE
        tvCurrentDate.visibility = View.GONE
        tvSectionInfo.text = btnMostPopularThisDay.text

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            val intent = Intent(this, SingUpActivity::class.java)
            startActivity(intent)

            val url = "https://api.themoviedb.org/3/trending/tv/day?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

            val request = JsonObjectRequest(Request.Method.GET ,url, null,
                { response ->
                    val serials = parseResponse(response)

                    val pages = response.getInt("total_pages")

                    if (currentPage == pages) {
                        isLastPage = true
                    } else {
                        isLastPage = false
                    }

                    serialsList.clear()
                    serialsList.addAll(serials)
                    adapter.notifyDataSetChanged()

                },
                { error ->
                    error.printStackTrace()
                })

            Volley.newRequestQueue(this).add(request)

            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.GONE

            tvSectionInfo.text = btnMostPopularThisDay.text

            recyclerView.scrollToPosition(0)

            currentCategory = 0 // Ustawia kategorię na Trendujące seriale (0)

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START) // Zamknij wysuwane okno boczne po wybraniu opcji
        }
        else
        {
            val url = "https://api.themoviedb.org/3/trending/tv/day?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

            val request = JsonObjectRequest(Request.Method.GET ,url, null,
                { response ->
                    val serials = parseResponse(response)

                    val pages = response.getInt("total_pages")

                    if (currentPage == pages) {
                        isLastPage = true
                    } else {
                        isLastPage = false
                    }

                    serialsList.clear()
                    serialsList.addAll(serials)
                    adapter.notifyDataSetChanged()

                },
                { error ->
                    error.printStackTrace()
                })

            Volley.newRequestQueue(this).add(request)

            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.GONE

            tvSectionInfo.text = btnMostPopularThisDay.text

            recyclerView.scrollToPosition(0)

            currentCategory = 0 // Ustawia kategorię na Trendujące seriale (0)

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START) // Zamknij wysuwane okno boczne po wybraniu opcji
        }

        btnZaloguj.setOnClickListener {

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)

        }

        btnWatchlist.setOnClickListener {

            currentCategory = 10

            btnFilterButton.visibility = View.GONE
            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.GONE

            tvSectionInfo.text = btnWatchlist.text
            serialsList.clear()
            fetchSerialDetails()
            recyclerView.scrollToPosition(0)
            drawerLayout.closeDrawer(GravityCompat.START)

        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE
                    ) {
                        currentPage++
                        if(currentCategory == 4)
                        {
                            onFilterApplied(selYear, selSort, selVote, selProvider, selGenre, currentPage)
                        }
                        else {
                            fetchDataForPage(currentPage, currentCategory, currentDate)
                        }
                    }
                }
            }
        })


        btnFilterButton.setOnClickListener {

            showFiltersDialog()
            adapter.notifyDataSetChanged()

        }

        etSearch.setOnKeyListener{v, keyCode, event ->
            when{
                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) ->
                {
                    serialsList.clear()
                    performSearch(etSearch.text.toString())
                    drawerLayout.closeDrawer(GravityCompat.START)
                    tvSectionInfo.text = etSearch.text.toString()
                    etSearch.text.clear()
                    btnFilterButton.visibility = View.GONE
                    btnPrevDayButton.visibility = View.GONE
                    btnNextDayButton.visibility = View.GONE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.GONE
                    btnFilterButton.visibility = View.GONE

                    currentCategory = 6

                    recyclerView.scrollToPosition(0)

                    return@setOnKeyListener true
                }
                else -> false
            }
        }

        btnFiltry.setOnClickListener {

            loadingSpinner.visibility = View.VISIBLE

            val url = "https://api.themoviedb.org/3/tv/top_rated?language=pl-PL&page=1&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

            val request = JsonObjectRequest(
                Request.Method.GET,url, null,
                { response ->
                    val serials = parseResponse(response)

                    val pages = response.getInt("total_pages")

                    if (currentPage == pages) {
                        isLastPage = true
                    } else {
                        isLastPage = false
                    }

                    serialsList.clear()
                    serialsList.addAll(serials)
                    adapter.notifyDataSetChanged()
                    loadingSpinner.visibility = View.GONE
                },
                { error ->
                    error.printStackTrace()
                })

            Volley.newRequestQueue(this).add(request)

            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.VISIBLE
            tvSectionInfo.text = btnFiltry.text

            currentCategory = 4

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START)

            loadingSpinner.visibility = View.GONE
        }

        btnNoweOdcinki.setOnClickListener {

            fetchDataForDateNoweOdcinki(currentDate)

            isPremiery = 0

            btnPrevDayButton.visibility = View.VISIBLE
            btnNextDayButton.visibility = View.VISIBLE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.VISIBLE
            btnFilterButton.visibility = View.GONE

            recyclerView.scrollToPosition(0)

            tvSectionInfo.text = btnNoweOdcinki.text

            currentCategory = 3

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START)

            adapter.notifyDataSetChanged()

        }

        btnMostPopularThisDay.setOnClickListener{

            val url = "https://api.themoviedb.org/3/trending/tv/day?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

            val request = JsonObjectRequest(Request.Method.GET ,url, null,
                { response ->
                    val serials = parseResponse(response)

                    val pages = response.getInt("total_pages")

                    if (currentPage == pages) {
                        isLastPage = true
                    } else {
                        isLastPage = false
                    }

                    serialsList.clear()
                    serialsList.addAll(serials)
                    adapter.notifyDataSetChanged()

                },
                { error ->
                    error.printStackTrace()
                })

            Volley.newRequestQueue(this).add(request)

            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.GONE

            tvSectionInfo.text = btnMostPopularThisDay.text

            recyclerView.scrollToPosition(0)

            currentCategory = 0 // Ustawia kategorię na Trendujące seriale (0)

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START) // Zamknij wysuwane okno boczne po wybraniu opcji
        }

        btnMostPopularThisWeek.setOnClickListener{

            val url = "https://api.themoviedb.org/3/trending/tv/week?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

            val request = JsonObjectRequest(Request.Method.GET ,url, null,
                { response ->
                    val serials = parseResponse(response)

                    val pages = response.getInt("total_pages")

                    if (currentPage == pages) {
                        isLastPage = true
                    } else {
                        isLastPage = false
                    }
                    serialsList.clear()
                    serialsList.addAll(serials)
                    adapter.notifyDataSetChanged()
                },
                { error ->
                    error.printStackTrace()
                })

            Volley.newRequestQueue(this).add(request)

            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.GONE

            tvSectionInfo.text = btnMostPopularThisWeek.text

            recyclerView.scrollToPosition(0)

            currentCategory = 1 // Ustawia kategorię na Popularne seriale (1)

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START)


        }

        btnPremiery.setOnClickListener{

            currentCategory = 5

            fetchDataForDatePremiery(currentDate)

            isPremiery = 1

            resetPageNumber()

            btnPrevDayButton.visibility = View.VISIBLE
            btnNextDayButton.visibility = View.VISIBLE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.VISIBLE
            btnFilterButton.visibility = View.GONE

            recyclerView.scrollToPosition(0)

            tvSectionInfo.text = btnPremiery.text

            drawerLayout.closeDrawer(GravityCompat.START)
        }

        calendarLayout.layoutAnimation

        mostPopularSeriesButton.setOnClickListener {

            val url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date_year=2024&include_adult=false&language=pl-PL&page=1&sort_by=vote_count.desc&with_poster=true"

            val request = JsonObjectRequest(Request.Method.GET ,url, null,
                { response ->
                    val serials = parseResponse(response)

                    val pages = response.getInt("total_pages")

                    if (currentPage == pages) {
                        isLastPage = true
                    } else {
                        isLastPage = false
                    }

                    serialsList.clear()
                    serialsList.addAll(serials)
                    adapter.notifyDataSetChanged()
                },
                { error ->
                    error.printStackTrace()
                })

            Volley.newRequestQueue(this).add(request)

            btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE
            btnFilterButton.visibility = View.GONE

            tvSectionInfo.text = mostPopularSeriesButton.text

            recyclerView.scrollToPosition(0)

            currentCategory = 2

            resetPageNumber()

            drawerLayout.closeDrawer(GravityCompat.START) // Zamknij wysuwane okno boczne po wybraniu opcji
        }


        tvCurrentDate.setOnClickListener{
            isDatePickerVisible = !isDatePickerVisible

            if (isDatePickerVisible) {
                calendarLayout.visibility = View.VISIBLE
            } else {
                calendarLayout.visibility = View.GONE
            }
        }

        btnPrevDayButton.setOnClickListener{
            if (isPremiery == 1) {
                val previousDate = currentDate.minusDays(1)
                changeDate(previousDate)
                recyclerView.scrollToPosition(0)
                updateCalendarDate(previousDate)
            } else {
                val previousDate = currentDate.minusDays(1)
                changeDateForEpisodes(previousDate)
                resetPageNumber()
                recyclerView.scrollToPosition(0)
                updateCalendarDate(previousDate)
            }
        }


        btnNextDayButton.setOnClickListener{
            if (isPremiery == 1) {
                val nextDate = currentDate.plusDays(1)
                changeDate(nextDate)
                recyclerView.scrollToPosition(0)
                updateCalendarDate(nextDate)
            } else {
                val nextDate = currentDate.plusDays(1)
                changeDateForEpisodes(nextDate)
                resetPageNumber()
                recyclerView.scrollToPosition(0)
                updateCalendarDate(nextDate)
            }
        }

        datePicker.init(
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ) { view, year, monthOfYear, dayOfMonth ->
            if (isPremiery == 1) {
                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                changeDate(selectedDate)
                recyclerView.scrollToPosition(0)
            } else {
                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                changeDateForEpisodes(selectedDate)
                recyclerView.scrollToPosition(0)
            }
        }

        tvCurrentDate.text = LocalDate.now().toString()

    }

    private fun performSearch(query: String) {

        loadingSpinner.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.themoviedb.org/3/search/tv?query=$query&include_adult=false&language=pl-PL&page=1&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

        val request = JsonObjectRequest(url,{response ->

            val serials = parseResponse(response)

            val pages = response.getInt("total_pages")

            if (currentPage == pages) {
                isLastPage = true
            } else {
                isLastPage = false
            }

            serialsList.clear()
            serialsList.addAll(serials)
            adapter.notifyDataSetChanged()
            loadingSpinner.visibility = View.GONE

        }, { error ->

            error.printStackTrace()
            loadingSpinner.visibility = View.GONE

        })

        queue.add(request)

    }

    private fun showFiltersDialog() {
        val filtersDialogFragment = FiltersDiagloFragment(this@MainActivity, serialsList, recyclerView)
        resetPageNumber()
        adapter.notifyDataSetChanged()
        filtersDialogFragment.show(supportFragmentManager, "FiltersDialogFragment")
    }

    private fun fetchDataForPage(pageNumber: Int,  category: Int, selectedDate: LocalDate) {
        isLoading = true

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val newEpis = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&air_date.gte=${selectedDate.format(formatter)}&air_date.lte=${selectedDate.format(formatter)}&language=pl-PL&sort_by=vote_count.desc&watch_region=US&with_watch_providers=8|9|337|350|15|384|386|37|80&page="

        val url = when (category) {
            0 -> trendingTVDay
            1 -> trendingTVWeek
            2 -> trendingTV24
            3 -> newEpis
            6 -> ""
            10 -> ""
            else -> trendingTVDay
        } + pageNumber

        val request = JsonObjectRequest(Request.Method.GET ,url, null,
            { response ->

                val serials = parseResponse(response)
                val pages = response.getInt("total_pages")
                if (currentPage >= pages) {
                    isLastPage = true
                } else {
                    isLastPage = false
                }
                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
                isLoading = false
            },
            { error ->
                error.printStackTrace()
                isLoading = false
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun resetPageNumber() {
        if (currentPage != 1) {
            currentPage = 1
        }
    }

    private fun changeDateForEpisodes(selectedDate: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = selectedDate.format(formatter)
        tvCurrentDate.text = formattedDate
        currentDate = selectedDate
        fetchDataForDateNoweOdcinki(selectedDate)

    }

    private fun changeDate(selectedDate: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = selectedDate.format(formatter)
        tvCurrentDate.text = formattedDate
        currentDate = selectedDate
        fetchDataForDatePremiery(selectedDate)

    }

    private fun updateCalendarDate(date: LocalDate) {
        val year = date.year
        val month = date.monthValue - 1
        val dayOfMonth = date.dayOfMonth
        datePicker.updateDate(year, month, dayOfMonth)
    }

    private fun fetchDataForDateNoweOdcinki(selectedDate: LocalDate) {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&air_date.gte=${selectedDate.format(formatter)}&air_date.lte=${selectedDate.format(formatter)}&with_poster=true&language=pl-PL&sort_by=vote_count.desc&watch_region=US&with_watch_providers=8|9|337|350|15|384|386|37|80"

        val request = JsonObjectRequest(Request.Method.GET ,url, null,
            { response ->
                val serials = parseResponse(response)

                val pages = response.getInt("total_pages")

                if (currentPage == pages) {
                    isLastPage = true
                } else {
                    isLastPage = false
                }

                if(serials.isEmpty())
                {
                    currentPage++
                }

                serialsList.clear()
                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
            },
            { error ->
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchDataForDatePremiery(selectedDate: LocalDate) {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date.gte=${selectedDate.format(formatter)}&first_air_date.lte=${selectedDate.format(formatter)}&language=pl-PL"

        val request = JsonObjectRequest(
            Request.Method.GET,url, null,
            { response ->
                val serials = parseResponse(response)

                val pages = response.getInt("total_pages")

                if (currentPage == pages) {
                    isLastPage = true
                } else {
                    isLastPage = false
                }

                serialsList.clear()
                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
            },
            { error ->
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(request)
    }


    private fun parseResponse2(response: JSONObject): List<Serial> {
        val serials = mutableListOf<Serial>()

        val posterPath = response.getString("poster_path")
        val backdrop = response.getString("backdrop_path")
        val id = response.getInt("id")
        val title = response.getString("name")
        val releaseDate = response.getString("first_air_date")
        val overview = response.getString("overview")
        val userRating = response.getDouble("vote_average")
        val userRatingCount = response.getInt("vote_count")
        val backdropUrl = "https://image.tmdb.org/t/p/w500" + backdrop
        val posterUrl = "https://image.tmdb.org/t/p/w500" + posterPath

        serials.add(Serial( id ,title, releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))

        return serials
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

            serialId = id.toString()

            if(currentCategory == 5)
            {
                if(posterPath != "null" && overview != ""){
                serials.add(Serial( id ,title, releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))}

            }else{
                if(posterPath != "null")
                {
                    serials.add(Serial( id ,title, releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))
                }
            }

        }
        return serials
    }

    private fun mapSortCriteria(sortCriteria: String): String
    {
        val sortCriteriaMap = mapOf(
            "Najnowsze" to "first_air_date.desc",
            "Ilość głosów" to "vote_count.desc",
            "Popularność" to "popularity.desc",
            "Ocena" to "vote_average.desc"
        )

        return sortCriteriaMap[sortCriteria]?: sortCriteria
    }

    override fun onFilterApplied(year: String, sortCriteria: String, voteAmount: String, selectedProviders: List<String>, selectedGenres: List<String>, currentPage: Int) {

        isLoading = true

        val providerCodes = mapOf(
            "Netflix" to "8",
            "Amazon Prime" to "119",
            "Disney+" to "337",
            "Apple TV Plus" to "350",
            "Hbo Max" to "384",
            "SkyShowtime" to "1773"
        )
        val genresCodes = mapOf(
            "Akcja i przygoda" to "10759",
            "Animacja" to "16",
            "Komedia" to "35",
            "Kryminał" to "80",
            "Dokumentalny" to "99",
            "Dramat" to "18",
            "Sci-fi" to "10765",
            "Western" to "37"
        )
        val selectedProvidersCodes = selectedProviders.mapNotNull { providerCodes[it] }
        val providersString = selectedProvidersCodes.joinToString("|")
        val selectedGenresCodes = selectedGenres.mapNotNull { genresCodes[it] }
        val genresString = selectedGenresCodes.joinToString("|")

        val modifiedSortCriteria = mapSortCriteria(sortCriteria)

        selGenre = selectedGenres
        selProvider = selectedProviders
        selSort = sortCriteria
        selVote = voteAmount
        selYear = year

        val url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date_year=$year&include_adult=false&language=pl-PL&sort_by=$modifiedSortCriteria&with_poster=true&vote_count.gte=$voteAmount&watch_region=PL&with_watch_providers=$providersString&with_genres=$genresString&page=$currentPage"

        Log.d("Main Activity", "Provider call: $url")

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val serials = parseResponse(response)

                val pages = response.getInt("total_pages")
                isLastPage = currentPage == pages

                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
                isLoading = false
            },
            { error ->
                error.printStackTrace()
                isLoading = false
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchSerialDetails() {

        loadingSpinner.visibility = View.VISIBLE

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val collectionRef = db.collection("users").document(userId).collection("watchList")

        collectionRef.get()
            .addOnSuccessListener { documents ->
                val serialIds = mutableListOf<Int>()
                val serialsMap = mutableMapOf<Int, Long>()

                for (document in documents) {
                    val id = document.getLong("serialId")?.toInt()
                    val timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0
                    if (id != null) {
                        serialIds.add(id)
                        serialsMap[id] = timestamp
                    }
                }

                val serials = mutableListOf<Serial>()

                val requestsCount = serialIds.size
                var completedRequests = 0
                for (serialId in serialIds) {
                    val url = "https://api.themoviedb.org/3/tv/$serialId?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"
                    val request = JsonObjectRequest(
                        Request.Method.GET, url, null,
                        { response ->
                            val serial = parseResponse2(response)
                            serials.addAll(serial)

                            completedRequests++
                            if (completedRequests == requestsCount) {
                                serials.sortByDescending { serialsMap[it.id] }
                                serialsList.clear()
                                serialsList.addAll(serials)
                                adapter.notifyDataSetChanged()
                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        { error ->
                            error.printStackTrace()
                            completedRequests++
                            if (completedRequests == requestsCount) {
                                serials.sortByDescending { serialsMap[it.id] }
                                serialsList.clear()
                                serialsList.addAll(serials)
                                adapter.notifyDataSetChanged()
                                loadingSpinner.visibility = View.GONE
                            }
                        }
                    )

                    Volley.newRequestQueue(this@MainActivity).add(request)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

}