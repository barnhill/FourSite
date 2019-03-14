package com.pnuema.android.codingchallenge.mainscreen.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.api.LocationResultsListener
import com.pnuema.android.codingchallenge.fullmap.ui.FullMapActivity
import com.pnuema.android.codingchallenge.helpers.Errors
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
 * Also there is a full map floating action button to take the user to the full map
 * display of all search results
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
            if (viewModel.searchFilter.isBlank()) {
                main_swipe_refresh.isRefreshing = false
                return@setOnRefreshListener
            }

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

    /**
     * Sets the search query on the view model for safe keeping
     * Will show the refresh progress indicator or hide it based on if a query is being requested.  Will trigger a query
     * to be made if the provided query string isnt blank.
     *
     * @param query Query string which to get results for from the Foursquare API
     */
    @WorkerThread
    fun setSearchQuery(query: String) {
        viewModel.searchFilter = query
        searchView?.setQuery(query, true)

        runOnUiThread {
            if (query.isBlank()) {
                toggleEmptyState(true)
                clearLocationResults()
            } else {
                toggleEmptyState(false)
                makeLocationRequest(query)
            }

            setFullMapVisibleState()
        }
    }

    /**
     * Handle standard on back pressed events but intercept back presses
     * and clear out the search results first before exiting on a subsequent call
     */
    override fun onBackPressed() {
        searchView?.let {
            if (!it.isIconified) {
                it.isIconified = true
                it.onActionViewCollapsed()
                viewModel.searchFilter = ""
                clearLocationResults()
                return
            }
        }
        super.onBackPressed()
    }

    /**
     * Makes the request to get the locations for this query and will set the resulting list on the adapter which will update the displayed list
     */
    private fun makeLocationRequest(query: String) {
        if (query.isBlank()) {
            return
        }
        main_swipe_refresh.isRefreshing = true
        dismissSnackBar()
        requestor.getLocationResults(query, object : LocationResultsListener {
            override fun success(locations: ArrayList<LocationResult>) {
                main_swipe_refresh.isRefreshing = false
                setLocationResults(locations)
            }

            override fun failed() {
                //clear the results
                clearLocationResults()

                //cancel the progress indicator
                main_swipe_refresh.isRefreshing = false

                //show error message
                snackBar = Errors.showError(main_coordinator, R.string.request_failed_main, View.OnClickListener { makeLocationRequest(query) })
            }
        })
    }

    /**
     * Dismiss any error message that is showing
     */
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
        if (viewModel.locationResults.isEmpty()) {
            //empty or blank query so lets hide the full map button
            main_toggle_full_map.hide()
        } else {
            //results exist or the search query is not blank so
            main_toggle_full_map.show()
        }
    }

    /**
     * Set the location results on the view model,
     * refresh the adapter with the locations so it can display the list of locations,
     * and update the state of the floating action button for the full map
     */
    private fun setLocationResults(locations: ArrayList<LocationResult>) {
        viewModel.locationResults = locations
        adapter.setLocationResults(locations = viewModel.locationResults)
        setFullMapVisibleState()
    }

    /**
     * Clears the results saved to the view model,
     * refresh the adapter with no results,
     * and set the full map floating action button
     */
    private fun clearLocationResults() {
        viewModel.locationResults.clear()
        adapter.setLocationResults(locations = viewModel.locationResults)
        setFullMapVisibleState()
    }

    /**
     * Set screen to show the empty message and glyph or show the results and swipe refresh
     * True sets it to empty, false will display the results with no glyph shown
     */
    private fun toggleEmptyState(state: Boolean) {
        if (state) {
            main_swipe_refresh.visibility = View.GONE
            group_empty_data.visibility = View.VISIBLE
        } else {
            main_swipe_refresh.visibility = View.VISIBLE
            group_empty_data.visibility = View.GONE
        }
    }
}
