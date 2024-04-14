package com.example.tvcalendar

import SerialAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), CustomBottomSheetDialogFragment.OnApplyButtonClickListener {

    private lateinit var btnUndoButton: ImageView
    private lateinit var moreButton3: TextView
    private lateinit var moreButton2: TextView
    private lateinit var moreButton1: TextView
    private lateinit var hintsLayout: LinearLayout
    private lateinit var recyclerViewHorizontal3: RecyclerView
    private lateinit var recyclerViewHorizontal2: RecyclerView
    private lateinit var recyclerViewHorizontal: RecyclerView
    private lateinit var recylerViewGrid: RecyclerView
    private lateinit var daysLayout: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var mostPopularSeriesButton: TextView
    private lateinit var adapter: SerialAdapter
    private lateinit var adapterViewGrid: SerialGridAdapter
    private lateinit var adapterHorizontal: SerialHorizontalAdapter
    private lateinit var adapterHorizontal2: SerialHorizontalAdapter
    private lateinit var adapterHorizontal3: SerialHorizontalAdapter
    private lateinit var serialsListWatchlist : MutableList<Serial>
    private lateinit var serialsList4 : MutableList<Serial>
    private lateinit var serialsList3: MutableList<Serial>
    private lateinit var serialsList2: MutableList<Serial>
    private lateinit var serialsList: MutableList<Serial>
    private lateinit var datePicker: DatePicker
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvSectionInfo: TextView
    private lateinit var btnNextDayButton: ImageView
    private lateinit var btnPrevDayButton: ImageView
    private lateinit var calendarLayout: LinearLayout
    private lateinit var btnPremiery: Button
    private lateinit var layoutSearchHints: LinearLayout
    private lateinit var btnMostPopularThisWeek: TextView
    private lateinit var btnMostPopularThisDay: TextView
    private lateinit var layoutMain: LinearLayout
    private lateinit var btnNoweOdcinki: Button
    private lateinit var btnFilterButton: ImageView
    private lateinit var etSearch: EditText
    private lateinit var btnFiltry: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSortingButton: ImageView
    //private lateinit var btnZmienKonto: Button
    //private lateinit var btnWatchlist: Button
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var loadingSpinner2: ProgressBar
    private lateinit var tvName: TextView
    private lateinit var ivLogOut: ImageView
    private lateinit var tvEmailDetails: TextView
    private lateinit var tvWatchlistDetails: TextView
    private lateinit var tvBrakSeriali: ImageView
    private lateinit var topBar: LinearLayout
    private lateinit var ivKalendarz: ImageView
    private lateinit var tvSortingInfo: TextView
    private lateinit var ivWatchlist: ImageView
    private lateinit var btnDeleteText: ImageView
    private lateinit var sadFaceLayout: LinearLayout
    private var currentDate: LocalDate = LocalDate.now()
    private var isDatePickerVisible = false
    private var currentPage = 1
    private var currentCategory = 5
    private var isWatchlistEnabled: Boolean = false
    private var isPremiery = 1
    private var isLoading = false
    private var backPressedTime: Long = 0
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

        recylerViewGrid = findViewById(R.id.recyclerViewGrid)
        btnUndoButton = findViewById(R.id.btnUndoButton)
        moreButton1 = findViewById(R.id.moreButton1)
        moreButton2 = findViewById(R.id.moreButton2)
        moreButton3 = findViewById(R.id.moreButton3)
        hintsLayout = findViewById(R.id.hintsLayout)
        recyclerViewHorizontal3 = findViewById(R.id.recyclerViewHorizontal3)
        recyclerViewHorizontal2 = findViewById(R.id.recyclerViewHorizontal2)
        recyclerViewHorizontal = findViewById(R.id.recyclerViewHorizontal)
        ivWatchlist = findViewById(R.id.ivOczko)
        btnDeleteText = findViewById(R.id.btnDeleteText)
        daysLayout = findViewById(R.id.buttonsLayout)
        ivKalendarz = findViewById(R.id.ivKalendarz)
        topBar = findViewById(R.id.topBar)
        layoutSearchHints = findViewById(R.id.layoutSearchHints)
        recyclerView = findViewById(R.id.recyclerView)
        datePicker = findViewById(R.id.datePicker)
        layoutMain = findViewById(R.id.layoutMain)
        ivLogOut = findViewById(R.id.ivLogOut)
        tvEmailDetails = findViewById(R.id.tvEmailInfo)
        tvWatchlistDetails = findViewById(R.id.tvWatchlistInfo)
        btnSortingButton = findViewById(R.id.btnSortingButton)
        tvSortingInfo = findViewById(R.id.tvSortingInfo)
        //btnPremiery = findViewById(R.id.btnPremiery)
        //tvSectionInfo = findViewById(R.id.tvSectionInfo)
        //drawerLayout = findViewById(R.id.drawer_layout)
        mostPopularSeriesButton = findViewById(R.id.most_popular_series_button)
        btnNextDayButton = findViewById(R.id.nextDayButton)
        btnPrevDayButton = findViewById(R.id.previousDayButton)
        tvCurrentDate = findViewById(R.id.dateTextView)
        calendarLayout = findViewById(R.id.dateSelectionLayout)
        btnFilterButton = findViewById(R.id.btnFilterButton)
        sadFaceLayout = findViewById(R.id.sadFaceLayout)
        btnMostPopularThisWeek = findViewById(R.id.most_popular_this_week)
        btnMostPopularThisDay = findViewById(R.id.most_popular_this_day)
        //btnNoweOdcinki = findViewById(R.id.btnNoweOdcinki)
        etSearch = findViewById(R.id.etSearch)
        //btnFiltry = findViewById(R.id.btnFiltry)
        //btnZmienKonto = findViewById(R.id.btnZmienKonto)
        //btnWatchlist = findViewById(R.id.btnWatchlist)
        loadingSpinner = findViewById(R.id.progress_loader)
        loadingSpinner2 = findViewById(R.id.progress_loader2)
        //tvName = findViewById(R.id.tvName)
        tvBrakSeriali = findViewById(R.id.ivBrakSeriali)

        serialsList = mutableListOf()
        adapter = SerialAdapter(serialsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        serialsList2 = mutableListOf()
        adapterHorizontal = SerialHorizontalAdapter(serialsList2)
        recyclerViewHorizontal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHorizontal.adapter = adapterHorizontal

        serialsList3 = mutableListOf()
        adapterHorizontal2 = SerialHorizontalAdapter(serialsList3)
        recyclerViewHorizontal2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHorizontal2.adapter = adapterHorizontal2

        serialsList4 = mutableListOf()
        adapterHorizontal3 = SerialHorizontalAdapter(serialsList4)
        recyclerViewHorizontal3.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHorizontal3.adapter = adapterHorizontal3

        serialsListWatchlist = mutableListOf()
        adapterViewGrid = SerialGridAdapter(serialsListWatchlist)
        recylerViewGrid.layoutManager = GridLayoutManager(this, 3)
        recylerViewGrid.adapter = adapterViewGrid


        daysLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE

        val user = FirebaseAuth.getInstance().currentUser

        fetchDataForDateNoweOdcinki(currentDate)

        bottomNav = findViewById(R.id.bottom_navigation)

        btnSortingButton.setOnClickListener {
            showSortingOptionsDialog()
        }

        btnUndoButton.setOnClickListener {
            btnFilterButton.visibility = View.VISIBLE
            btnUndoButton.visibility = View.GONE
            etSearch.hint = "Wpisz nazwę serialu"
            hintsLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            serialsList.clear()
            adapter.notifyDataSetChanged()
        }

        moreButton1.setOnClickListener{
            btnUndoButton.visibility = View.VISIBLE
            btnFilterButton.visibility = View.GONE
            btnSortingButton.visibility = View.GONE
            etSearch.hint = "Najpopularniejsze dzisiaj"
            loadingSpinner.visibility = View.VISIBLE
            hintsLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

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
                    loadingSpinner.visibility = View.GONE

                },
                { error ->
                    error.printStackTrace()
                    loadingSpinner.visibility = View.GONE
                })

            Volley.newRequestQueue(this).add(request)

            recyclerView.scrollToPosition(0)

            currentCategory = 1 // Ustawia kategorię na Trendujące seriale (1)

            resetPageNumber()
        }

        moreButton2.setOnClickListener{
            btnUndoButton.visibility = View.VISIBLE
            btnFilterButton.visibility = View.GONE
            btnSortingButton.visibility = View.GONE
            etSearch.hint = "Najpopularniejsze premiery 2024"
            loadingSpinner.visibility = View.VISIBLE
            hintsLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

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
                    loadingSpinner.visibility = View.GONE

                },
                { error ->
                    error.printStackTrace()
                    loadingSpinner.visibility = View.GONE
                })

            Volley.newRequestQueue(this).add(request)

            recyclerView.scrollToPosition(0)

            currentCategory = 2 // Ustawia kategorię na Trendujące seriale (2)

            resetPageNumber()

        }

        moreButton3.setOnClickListener{

            loadingSpinner.visibility = View.VISIBLE
            btnUndoButton.visibility = View.VISIBLE
            btnFilterButton.visibility = View.GONE
            btnSortingButton.visibility = View.GONE
            etSearch.hint = "Najpopularniejsze tego tygodnia"
            hintsLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

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
                    loadingSpinner.visibility = View.GONE
                },
                { error ->
                    error.printStackTrace()
                    loadingSpinner.visibility = View.GONE
                })

            Volley.newRequestQueue(this).add(request)

            //tvSectionInfo.text = btnMostPopularThisWeek.text

            recyclerView.scrollToPosition(0)

            currentCategory = 3 // Ustawia kategorię na Popularne seriale (3)

            resetPageNumber()

        }

        btnDeleteText.setOnClickListener {
            btnSortingButton.visibility = View.GONE
            hintsLayout.visibility = View.VISIBLE
            etSearch.hint = "Wpisz nazwę serialu"
            etSearch.text.clear()
            serialsList.clear()
            recyclerView.scrollToPosition(0)
            adapter.notifyDataSetChanged()
            btnDeleteText.visibility = View.GONE
            btnFilterButton.visibility = View.VISIBLE
        }

        ivWatchlist.setOnClickListener {

            resetPageNumber()
            loadingSpinner.visibility = View.VISIBLE
            currentCategory = if (currentCategory == 5) 12 else 5
            CoroutineScope(Dispatchers.Main).launch {
                isWatchlistEnabled = !isWatchlistEnabled
                if(isWatchlistEnabled)
                {
                    fetchWatchlistSeriesInCalendar(fetchDataForDateNoweOdcinki2(currentDate))
                    adapter.notifyDataSetChanged()
                }
                else
                {
                    fetchDataForDateNoweOdcinki(currentDate)
                    adapter.notifyDataSetChanged()
                }
                Log.d("isWatchlistEnabled", isWatchlistEnabled.toString())
                ivWatchlist.setImageResource(if (isWatchlistEnabled) R.drawable.oczkofull else R.drawable.watchlist)
                loadingSpinner.visibility = View.GONE
            }
        }

        btnFilterButton.setOnClickListener {

            val bottomSheet = CustomBottomSheetDialogFragment(serialsList, recyclerView)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            adapter.notifyDataSetChanged()
        }

        ivLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_user -> {

                    recylerViewGrid.visibility = View.GONE

                    hintsLayout.visibility = View.GONE
                    daysLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    ivLogOut.visibility = View.VISIBLE
                    tvEmailDetails.visibility = View.VISIBLE
                    tvEmailDetails.text = user?.email
                    layoutSearchHints.visibility = View.GONE

                    topBar.visibility = View.GONE
                    etSearch.visibility = View.GONE
                    btnPrevDayButton.visibility = View.GONE
                    btnNextDayButton.visibility = View.GONE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.GONE

                    serialsList.clear()

                    adapter.notifyDataSetChanged()

                    true
                }
                R.id.navigation_search -> {

                    recylerViewGrid.visibility = View.GONE
                    btnSortingButton.visibility = View.GONE
                    etSearch.hint = "Wpisz nazwę serialu"
                    hintsLayout.visibility = View.VISIBLE
                    recyclerViewHorizontal.visibility = View.VISIBLE

                    val url = "https://api.themoviedb.org/3/trending/tv/day?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

                    val request = JsonObjectRequest(Request.Method.GET ,url, null,
                        { response ->
                            val serials = parseResponse(response)

                            /*val pages = response.getInt("total_pages")

                            if (currentPage == pages) {
                                isLastPage = true
                            } else {
                                isLastPage = false
                            }*/
                            //setSadFace(serials)
                            serialsList2.clear()
                            serialsList2.addAll(serials)
                            adapterHorizontal.notifyDataSetChanged()

                        },
                        { error ->
                            error.printStackTrace()
                        })

                    Volley.newRequestQueue(this).add(request)

                    val url2 = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date_year=2024&include_adult=false&language=pl-PL&page=1&sort_by=vote_count.desc&with_poster=true"

                    val request2 = JsonObjectRequest(Request.Method.GET ,url2, null,
                        { response ->
                            val serials = parseResponse(response)

                            //setSadFace(serials)
                            serialsList3.clear()
                            serialsList3.addAll(serials)
                            adapterHorizontal2.notifyDataSetChanged()
                        },
                        { error ->
                            error.printStackTrace()
                        })

                    Volley.newRequestQueue(this).add(request2)

                    val url3 = "https://api.themoviedb.org/3/trending/tv/week?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

                    val request3 = JsonObjectRequest(Request.Method.GET ,url3, null,
                        { response ->
                            val serials = parseResponse(response)

                            //setSadFace(serials)
                            serialsList4.clear()
                            serialsList4.addAll(serials)
                            adapterHorizontal3.notifyDataSetChanged()
                        },
                        { error ->
                            error.printStackTrace()
                        })

                    Volley.newRequestQueue(this).add(request3)

                    btnPrevDayButton.visibility = View.GONE
                    btnNextDayButton.visibility = View.GONE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.GONE
                    layoutSearchHints.visibility = View.GONE

                    etSearch.hint = ""
                    daysLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    ivLogOut.visibility = View.GONE
                    tvEmailDetails.visibility = View.GONE

                    etSearch.visibility = View.VISIBLE
                    topBar.visibility = View.VISIBLE

                    layoutSearchHints.visibility = View.GONE
                    btnPrevDayButton.visibility = View.GONE
                    btnNextDayButton.visibility = View.GONE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.GONE

                    currentPage = 1
                    resetPageNumber()

                    currentCategory = 7

                    serialsList.clear()

                    adapter.notifyDataSetChanged()

                    // Obsługa kliknięcia na element Dashboard
                    true
                }
                R.id.navigation_kalendarz -> {

                    recylerViewGrid.visibility = View.GONE

                    hintsLayout.visibility = View.GONE
                    DateManager.date = tvCurrentDate.text.toString()

                    daysLayout.visibility = View.VISIBLE
                    fetchDataForDateNoweOdcinki(currentDate)

                    recyclerView.visibility = View.VISIBLE
                    isPremiery = 0
                    topBar.visibility = View.GONE
                    layoutSearchHints.visibility = View.GONE

                    ivLogOut.visibility = View.GONE
                    tvEmailDetails.visibility = View.GONE
                    etSearch.visibility = View.GONE
                    btnPrevDayButton.visibility = View.VISIBLE
                    btnNextDayButton.visibility = View.VISIBLE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.VISIBLE
                    //btnFilterButton.visibility = View.GONE

                    recyclerView.scrollToPosition(0)

                    currentCategory = 5

                    resetPageNumber()

                    adapter.notifyDataSetChanged()

                    true
                }
                R.id.navigation_watchlist -> {

                    hintsLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    recylerViewGrid.visibility = View.VISIBLE

                    DateManager.date = LocalDate.now().toString()

                    daysLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    currentCategory = 10
                    topBar.visibility = View.GONE
                    layoutSearchHints.visibility = View.GONE

                    etSearch.visibility = View.GONE
                    ivLogOut.visibility = View.GONE
                    tvEmailDetails.visibility = View.GONE
                    //btnFilterButton.visibility = View.GONE
                    btnPrevDayButton.visibility = View.GONE
                    btnNextDayButton.visibility = View.GONE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.GONE
                    //btnFilterButton.visibility = View.GONE
                    serialsList.clear()
                    fetchSerialDetails()
                    recyclerView.scrollToPosition(0)

                    true
                }
                else -> false
            }
        }

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
                    setSadFace(serials)
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
            //btnFilterButton.visibility = View.GONE

            //tvSectionInfo.text = btnMostPopularThisDay.text

            recyclerView.scrollToPosition(0)

            currentCategory = 0 // Ustawia kategorię na Trendujące seriale (0)

            resetPageNumber()

            //drawerLayout.closeDrawer(GravityCompat.START) // Zamknij wysuwane okno boczne po wybraniu opcji
        }
        else
        {
            /*val url = "https://api.themoviedb.org/3/trending/tv/day?language=pl-PL&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"

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

            Volley.newRequestQueue(this).add(request)*/

            /*btnPrevDayButton.visibility = View.GONE
            btnNextDayButton.visibility = View.GONE
            calendarLayout.visibility = View.GONE
            tvCurrentDate.visibility = View.GONE*/
            //btnFilterButton.visibility = View.GONE

            //tvSectionInfo.text = btnMostPopularThisDay.text

            /*recyclerView.scrollToPosition(0)

            currentCategory = 0 // Ustawia kategorię na Trendujące seriale (0)

            resetPageNumber()*/

            //drawerLayout.closeDrawer(GravityCompat.START) // Zamknij wysuwane okno boczne po wybraniu opcji
        }

        /*btnZmienKonto.setOnClickListener {

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }*/

        /*btnWatchlist.setOnClickListener {

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

        }*/

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
                    ) {
                        currentPage++

                        if(currentCategory == 2)
                        {
                            var query = ApiQueryManager.query

                            performSearch(query, currentPage)
                        }

                        if(currentCategory == 7)
                        {
                            fetchDataForPage(currentPage, currentCategory, currentDate)
                        }
                        if(currentCategory == 6)
                        {
                            fetchSerialsBySort(selSort, currentPage)
                        }
                        else {
                            fetchDataForPage(currentPage, currentCategory, currentDate)
                        }
                    }
                }
            }
        })

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnDeleteText.visibility = View.VISIBLE
                btnSortingButton.visibility = View.GONE
                btnFilterButton.visibility = View.GONE

                if(etSearch.text.isEmpty())
                {
                    btnDeleteText.visibility = View.GONE
                    btnSortingButton.visibility = View.VISIBLE
                    btnFilterButton.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        etSearch.setOnKeyListener{v, keyCode, event ->
            when{
                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) ->
                {
                    hintsLayout.visibility = View.GONE
                    btnDeleteText.visibility = View.VISIBLE
                    resetPageNumber()
                    btnSortingButton.visibility = View.GONE
                    btnFilterButton.visibility = View.GONE
                    serialsList.clear()
                    recyclerView.visibility = View.VISIBLE
                    performSearch(etSearch.text.toString(), currentPage)
                    ApiQueryManager.query = etSearch.text.toString()
                    //btnFilterButton.visibility = View.GONE
                    btnPrevDayButton.visibility = View.GONE
                    btnNextDayButton.visibility = View.GONE
                    calendarLayout.visibility = View.GONE
                    tvCurrentDate.visibility = View.GONE
                    //btnFilterButton.visibility = View.GONE

                    currentCategory = 2

                    recyclerView.scrollToPosition(0)

                    return@setOnKeyListener true
                }
                else -> false
            }
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
                    setSadFace(serials)
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
            layoutSearchHints.visibility = View.GONE

            //tvSectionInfo.text = btnMostPopularThisDay.text

            recyclerView.scrollToPosition(0)

            currentCategory = 0 // Ustawia kategorię na Trendujące seriale (0)

            resetPageNumber()
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
                    setSadFace(serials)
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
            layoutSearchHints.visibility = View.GONE

            //tvSectionInfo.text = btnMostPopularThisWeek.text

            recyclerView.scrollToPosition(0)

            currentCategory = 1 // Ustawia kategorię na Popularne seriale (1)

            resetPageNumber()

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

                    setSadFace(serials)
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
            layoutSearchHints.visibility = View.GONE

            //tvSectionInfo.text = mostPopularSeriesButton.text

            recyclerView.scrollToPosition(0)

            currentCategory = 2

            resetPageNumber()
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
            CoroutineScope(Dispatchers.Main).launch {
                val previousDate = currentDate.minusDays(1)
                if(isWatchlistEnabled){
                    changeDateForEpisodes2(previousDate)
                }
                else
                {
                    changeDateForEpisodes(previousDate)
                }
                resetPageNumber()
                recyclerView.scrollToPosition(0)
                adapter.notifyDataSetChanged()
            }
        }



            btnNextDayButton.setOnClickListener{
                CoroutineScope(Dispatchers.Main).launch {
                val nextDate = currentDate.plusDays(1)
                if(isWatchlistEnabled){
                    changeDateForEpisodes2(nextDate)
                }
                else{
                    changeDateForEpisodes(nextDate)
                }
                resetPageNumber()
                recyclerView.scrollToPosition(0)
                adapter.notifyDataSetChanged()

            }
        }

        datePicker.init(
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ) { view, year, monthOfYear, dayOfMonth ->

                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)

                changeDateForEpisodes(selectedDate)
                resetPageNumber()
                recyclerView.scrollToPosition(0)
                adapter.notifyDataSetChanged()

        }

        tvCurrentDate.text = LocalDate.now().toString()

        DateManager.date = tvCurrentDate.text.toString()

    }
    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "Naciśnij ponownie aby wyjść", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
    private suspend fun fetchWatchlistSeriesInCalendar(serials: List<Serial>): List<Serial> {

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val watchlistRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("watchList")
        val watchlistSnapshot = watchlistRef.get().await()
        val watchlistIds = watchlistSnapshot.documents.map { it.id }

        val filteredSeries = serials.filter { series ->
            watchlistIds.any { it == series.id.toString() }
        }

        return filteredSeries
    }
    private fun performSearch(query: String, currentPage: Int) {

        loadingSpinner.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.themoviedb.org/3/search/tv?query=$query&include_adult=false&language=pl-PL&page=$currentPage&api_key=666a73d3efd51c4e1b78be4cd2c7a8ce"
        ApiQueryManager.url = url

        val request = JsonObjectRequest(url,{response ->

            Log.d("Provider call2", url)

            val serials = parseResponse(response)

            val pages = response.getInt("total_pages")

            if (currentPage == pages) {
                isLastPage = true
            } else {
                isLastPage = false
            }
            setSadFace(serials)
            serialsList.addAll(serials)
            adapter.notifyDataSetChanged()
            loadingSpinner.visibility = View.GONE

        }, { error ->

            error.printStackTrace()
            loadingSpinner.visibility = View.GONE

        })

        queue.add(request)

    }
    private fun fetchDataForPage(pageNumber: Int,  category: Int, selectedDate: LocalDate) {

        loadingSpinner.visibility = View.VISIBLE

        isLoading = true

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val newEpis = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&air_date.gte=${selectedDate.format(formatter)}&air_date.lte=${selectedDate.format(formatter)}&language=pl-PL&watch_region=US&with_original_language=en&page="

        val url = when (category) {
            1 -> trendingTVDay
            3 -> trendingTVWeek
            2 -> trendingTV24
            5 -> newEpis
            6 -> ""
            7 -> ApiQueryManager.url + "&page="
            10 -> ""
            12 -> ""
            else -> ""
        } + pageNumber

       Log.d("Odcinki page Main", url)

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
                loadingSpinner.visibility = View.GONE
                isLoading = false
            },
            { error ->
                error.printStackTrace()
                loadingSpinner.visibility = View.GONE
                isLoading = false
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun resetPageNumber() {
        if (currentPage != 1) {
            currentPage = 1
        }
    }

    private fun changeDateForEpisodes2(selectedDate: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = selectedDate.format(formatter)
        tvCurrentDate.text = formattedDate
        DateManager.date = tvCurrentDate.text.toString()
        Log.d("DateManager", DateManager.date)
        currentDate = selectedDate
        CoroutineScope(Dispatchers.Main).launch {
            fetchDataForDateNoweOdcinki2(selectedDate)
        }

    }

    private fun changeDateForEpisodes(selectedDate: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = selectedDate.format(formatter)
        tvCurrentDate.text = formattedDate
        DateManager.date = tvCurrentDate.text.toString()
        Log.d("DateManager", DateManager.date)
        currentDate = selectedDate
        fetchDataForDateNoweOdcinki(selectedDate)

    }

    private fun updateCalendarDate(date: LocalDate) {
        val year = date.year
        val month = date.monthValue - 1
        val dayOfMonth = date.dayOfMonth
        datePicker.updateDate(year, month, dayOfMonth)
    }

    private fun fetchDataForDateNoweOdcinki(selectedDate: LocalDate) {

        loadingSpinner.visibility = View.VISIBLE

        btnPrevDayButton.isEnabled = false
        btnNextDayButton.isEnabled = false

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&air_date.gte=${selectedDate.format(formatter)}&air_date.lte=${selectedDate.format(formatter)}&with_poster=true&language=pl-PL&watch_region=US&with_original_language=en&page=1"

        val request = JsonObjectRequest(Request.Method.GET ,url, null,
            { response ->

                Log.d("Provider call5", url)
                val serials = parseResponse(response)

                val pages = response.getInt("total_pages")

                if (currentPage == pages) {
                    isLastPage = true
                } else {
                    isLastPage = false
                }

                setSadFace(serials)

                serialsList.clear()
                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
                loadingSpinner.visibility = View.GONE
                btnPrevDayButton.isEnabled = true
                btnNextDayButton.isEnabled = true
            },
            { error ->
                error.printStackTrace()
                loadingSpinner.visibility = View.GONE
                btnPrevDayButton.isEnabled = true
                btnNextDayButton.isEnabled = true
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchDataForDateNoweOdcinki2(selectedDate: LocalDate, page: Int = 1, clearList: Boolean = false): List<Serial> {

        if (page == 1 || clearList) {
            serialsList.clear()
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val url =
            "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&air_date.gte=${
                selectedDate.format(formatter)
            }&air_date.lte=${selectedDate.format(formatter)}&with_poster=true&language=pl-PL&watch_region=US&with_original_language=en&page=$page"

        Log.d("Odcinki", "Odcinki: $url")

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val serials = parseResponse(response)

                val totalPages = response.getInt("total_pages")

                if (page < totalPages) {
                    fetchDataForDateNoweOdcinki2(
                        selectedDate,
                        page + 1
                    ) // Rekurencyjne pobieranie danych ze wszystkich stron
                }

                if (currentPage == totalPages) {
                    isLastPage = true
                } else {
                    isLastPage = false
                }

                if (serials.isEmpty()) {
                    currentPage++
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val filteredSerials = fetchWatchlistSeriesInCalendar(serials)
                    serialsList.addAll(filteredSerials)
                    adapter.notifyDataSetChanged()
                }

            },
            { error ->
                error.printStackTrace()
                btnNextDayButton.isEnabled = true
                btnPrevDayButton.isEnabled = true
            })

        Volley.newRequestQueue(this@MainActivity).add(request)

        return serialsList
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
        val originalName = response.getString("original_name")

        serials.add(Serial( id ,title, originalName ,releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))

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
            val originalName = serialJson.getString("original_name")

            serialId = id.toString()

            if(currentCategory == 3)
            {
                if(posterPath != "null" && overview != ""){
                serials.add(Serial( id ,title, originalName, releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))}

            }else{
                if(posterPath != "null" && overview != "")
                {
                    serials.add(Serial( id ,title, originalName, releaseDate, overview, posterUrl, userRating, userRatingCount, backdropUrl, false))
                }
            }

        }
        return serials
    }

    private fun fetchSerialDetails() {

        recylerViewGrid.visibility = View.VISIBLE

        loadingSpinner.visibility = View.VISIBLE

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val collectionRef = db.collection("users").document(userId).collection("watchList")

        collectionRef.get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {
                    sadFaceLayout.visibility = View.VISIBLE
                    loadingSpinner.visibility = View.GONE
                    return@addOnSuccessListener
                }

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
                                setSadFace(serials)
                                serialsListWatchlist.clear()
                                serialsListWatchlist.addAll(serials)
                                adapterViewGrid.notifyDataSetChanged()
                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        { error ->
                            error.printStackTrace()
                            completedRequests++
                            if (completedRequests == requestsCount) {
                                serials.sortByDescending { serialsMap[it.id] }
                                setSadFace(serials)
                                serialsListWatchlist.clear()
                                serialsListWatchlist.addAll(serials)
                                adapterViewGrid.notifyDataSetChanged()
                                loadingSpinner.visibility = View.GONE
                            }
                        }
                    )

                    Volley.newRequestQueue(this@MainActivity).add(request)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                loadingSpinner.visibility = View.GONE
            }
    }
    override fun onApplyButtonClick(
        genres: List<String>,
        channels: List<String>,
        votes: String,
        year: String
    ) {

        hintsLayout.visibility = View.GONE

        resetPageNumber()

        etSearch.hint = ""

        currentCategory = 7

        loadingSpinner.visibility = View.VISIBLE

        recyclerView.visibility = View.VISIBLE

        btnSortingButton.visibility = View.VISIBLE

        isLoading = true

        layoutSearchHints.visibility = View.GONE

        Log.d("MainActivityAction", "Apply button clicked. Genres: $genres, Channels: $channels, Votes: $votes, Year: $year")

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
        val selectedProvidersCodes = channels.mapNotNull { providerCodes[it] }
        val providersString = selectedProvidersCodes.joinToString("|")
        val selectedGenresCodes = genres.mapNotNull { genresCodes[it] }
        val genresString = selectedGenresCodes.joinToString("|")

        selGenre = genres
        selProvider = channels
        selVote = votes
        selYear = year

        ApiQueryManager.url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date_year=$year&vote_count.gte=$votes&include_adult=false&language=pl-PL&with_poster=true&watch_region=PL&with_watch_providers=$providersString&with_genres=$genresString"

        val url = "https://api.themoviedb.org/3/discover/tv?api_key=666a73d3efd51c4e1b78be4cd2c7a8ce&first_air_date_year=$year&vote_count.gte=$votes&include_adult=false&language=pl-PL&with_poster=true&watch_region=PL&with_watch_providers=$providersString&with_genres=$genresString&page=1"

        Log.d("Main Activity", "Provider call: $url")

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val serials = parseResponse(response)

                setSadFace(serials)

                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
                loadingSpinner.visibility = View.GONE

                isLoading = false
            },
            { error ->
                error.printStackTrace()
                isLoading = false
                loadingSpinner.visibility = View.GONE
            })

        Volley.newRequestQueue(this).add(request)

    }

    private fun setSadFace(serialsList: List<Serial>) {

        if(serialsList.isEmpty())
        {
            sadFaceLayout.visibility = View.VISIBLE
        }
        else
        {
            sadFaceLayout.visibility = View.GONE
        }
    }

    private fun showSortingOptionsDialog() {

        val sortingOptions = arrayOf("Po najnowszych", "Po ilości głosów", "Po ocenach", "Po popularności")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wybierz kryterium sortowania")
            .setItems(sortingOptions) { dialog, which ->
                when (which) {
                    0 -> {
                        loadingSpinner.visibility = View.VISIBLE
                        currentPage=1
                        etSearch.hint = "sortuj po: najnowszych"
                        serialsList.clear()
                        currentCategory=6
                        recyclerView.scrollToPosition(0)
                        fetchSerialsBySort("first_air_date.desc",1)
                        loadingSpinner.visibility = View.GONE}
                    1 -> {
                        loadingSpinner.visibility = View.VISIBLE
                        currentPage=1
                        etSearch.hint = "sortuj po: ilości głosów"
                        serialsList.clear()
                        currentCategory=6
                        recyclerView.scrollToPosition(0)
                        fetchSerialsBySort("vote_count.desc",1)
                        loadingSpinner.visibility = View.GONE}
                    2 -> {
                        loadingSpinner.visibility = View.VISIBLE
                        currentPage=1
                        etSearch.hint = "sortuj po: ocenach"
                        serialsList.clear()
                        currentCategory=6
                        recyclerView.scrollToPosition(0)
                        fetchSerialsBySort("vote_average.desc",1)
                        loadingSpinner.visibility = View.GONE}
                    3 -> {
                        loadingSpinner.visibility = View.VISIBLE
                        currentPage=1
                        etSearch.hint = "sortuj po: popularności"
                        serialsList.clear()
                        currentCategory=6
                        recyclerView.scrollToPosition(0)
                        fetchSerialsBySort("popularity.desc",1)
                        loadingSpinner.visibility = View.GONE}
                }
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun fetchSerialsBySort(sort: String, currentPage: Int) {

        loadingSpinner.visibility = View.VISIBLE

        isLoading = true

        selSort = sort

        val url = ApiQueryManager.url + "&sort_by=$sort" + "&page=$currentPage"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->

                Log.d("Main Activity", "Provider call3: $url")
                val serials = parseResponse(response)

                val pages = response.getInt("total_pages")
                isLastPage = currentPage == pages

                setSadFace(serials)

                serialsList.addAll(serials)
                adapter.notifyDataSetChanged()
                loadingSpinner.visibility = View.GONE
                isLoading = false
            },
            { error ->
                error.printStackTrace()
                isLoading = false
                loadingSpinner.visibility = View.GONE
            })

        Volley.newRequestQueue(this).add(request)
    }
}