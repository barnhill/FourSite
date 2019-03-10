package com.pnuema.android.codingchallenge.mainscreen.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.api.LocationResultsListener
import com.pnuema.android.codingchallenge.fullmap.ui.FullMapActivity
import com.pnuema.android.codingchallenge.mainscreen.requests.SearchRequest
import com.pnuema.android.codingchallenge.mainscreen.ui.adapters.SearchResultsAdapter
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import com.pnuema.android.codingchallenge.mainscreen.ui.viewmodels.MainScreenViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.TimeUnit

/**
 * Main screen of the app with search capabilities
 *
 * Displays the list of search results and allows the user to favorite their venues.
 */
class MainActivity : AppCompatActivity() {
    companion object {
        const val STATE_QUERY_STRING = "queryString"
    }
    private lateinit var viewModel: MainScreenViewModel
    private lateinit var requestor: SearchRequest
    private lateinit var adapter: SearchResultsAdapter
    private var snackBar: Snackbar? = null
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get<MainScreenViewModel>(MainScreenViewModel::class.java)
        requestor = SearchRequest()
        adapter = SearchResultsAdapter()
        main_locations_recycler.adapter = adapter

        savedInstanceState?.let {
            val query = it.getString(STATE_QUERY_STRING)
            query?.let { queryString ->
                viewModel.searchFilter = queryString
            }
        }

        main_swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))

        main_swipe_refresh.setOnRefreshListener {
            makeLocationRequest(viewModel.searchFilter)
        }

        main_toggle_full_map.setOnClickListener {
            FullMapActivity.launch(this, viewModel.locationResults)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_item_location_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        menu?.findItem(R.id.menu_item_location_search)?.let {
            (it.actionView as? SearchView).let { searchView ->
                searchView?.let { view ->
                    this.searchView = searchView
                    view.maxWidth = Integer.MAX_VALUE // to make sure it occupies the entire screen width as possible.
                    view.queryHint = getString(com.pnuema.android.codingchallenge.R.string.search_locations)

                    //populate the search filter if this view model already has the filter set
                    //(previous search stored in view model and restoration of screen state)
                    if (!viewModel.searchFilter.isBlank()) {
                        view.onActionViewExpanded()
                        setSearchQuery(viewModel.searchFilter)
                    } else {
                        view.onActionViewCollapsed()
                    }

                    //watch for more user input before firing off the api requests to cut down on traffic
                    Observable.create(ObservableOnSubscribe<String> { subscriber ->
                        view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String): Boolean {
                                subscriber.onNext(query)
                                return true
                            }

                            override fun onQueryTextChange(query: String): Boolean {
                                //set the new search query on the view model to preserve state and update the 'source of truth'
                                subscriber.onNext(query)
                                return true
                            }
                        })
                    })
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .subscribe{ query ->
                            setSearchQuery(query)
                        }
                }
            }
        }

        return true
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putString(STATE_QUERY_STRING, viewModel.searchFilter)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    @WorkerThread
    fun setSearchQuery(query: String) {
        viewModel.searchFilter = query
        searchView?.setQuery(query, true)

        runOnUiThread {
            if (query.isBlank()) {
                main_swipe_refresh.visibility = View.GONE
                group_empty_data.visibility = View.VISIBLE
            } else {
                main_swipe_refresh.visibility = View.VISIBLE
                group_empty_data.visibility = View.GONE
                makeLocationRequest(query)
            }

            setFullMapVisibleState()
        }
    }

    override fun onBackPressed() {
        searchView?.let {
            if (!it.isIconified) {
                it.isIconified = true
                it.onActionViewCollapsed()
                return
            }
        }
        super.onBackPressed()
    }

    /**
     * Makes the request to get the locations for this query and will set the resulting list on the adapter which will update the displayed list
     */
    private fun makeLocationRequest(query: String) {
        main_swipe_refresh.isRefreshing = true
        dismissSnackBar()
        requestor.getLocationResults(query, object : LocationResultsListener {
            override fun success(locations: ArrayList<LocationResult>) {
                main_swipe_refresh.isRefreshing = false
                adapter.setLocationResults(locations = locations)
                viewModel.locationResults = locations
                setFullMapVisibleState()
            }

            override fun failed() {
                main_swipe_refresh.isRefreshing = false
                snackBar = Snackbar.make(findViewById<View>(android.R.id.content), R.string.request_failed, Snackbar.LENGTH_INDEFINITE)
                snackBar?.let {
                    it.setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                    it.setAction(getString(R.string.retry)) { makeLocationRequest(query) }
                    it.show()
                }
            }
        })
    }

    private fun dismissSnackBar() {
        snackBar?.let {
            it.dismiss()
            snackBar = null
        }
    }

    /**
     * Check if the query is empty or not and animate the full map action button to the proper location
     */
    @UiThread
    private fun setFullMapVisibleState() {
        main_toggle_full_map.isVisible = true
        if (viewModel.locationResults.isEmpty() || viewModel.searchFilter.isBlank()) {
            //empty or blank query so lets hide the full map button
            val bottomMargin = (main_toggle_full_map.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin
            main_toggle_full_map.animate().translationY(main_toggle_full_map.height.toFloat() + bottomMargin).setInterpolator(AccelerateInterpolator(2f)).start()
        } else {
            //query is populated so show the action button
            main_toggle_full_map.animate().translationY(0f).setInterpolator(DecelerateInterpolator(2f)).start()
        }
    }
}
