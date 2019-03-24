package com.pnuema.android.codingchallenge.mainscreen.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.details.ui.DetailsActivity
import com.pnuema.android.codingchallenge.fullmap.ui.FullMapActivity
import com.pnuema.android.codingchallenge.helpers.Errors
import com.pnuema.android.codingchallenge.mainscreen.ui.adapters.SearchResultsAdapter
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import com.pnuema.android.codingchallenge.mainscreen.ui.viewholders.LocationClickListener
import com.pnuema.android.codingchallenge.mainscreen.ui.viewmodels.MainScreenViewModel
import com.pnuema.android.codingchallenge.persistance.FavoritesDatabase
import com.pnuema.android.codingchallenge.persistance.daos.Favorite
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Main screen of the app with search capabilities
 *
 * Displays the list of search results and allows the user to favorite their venues.
 * Also there is a full map floating action button to take the user to the full map
 * display of all search results
 */
class MainActivity : AppCompatActivity(), LifecycleOwner, LocationClickListener {
    companion object {
        const val STATE_QUERY_STRING = "queryString"
    }
    private lateinit var viewModel: MainScreenViewModel
    private lateinit var adapter: SearchResultsAdapter
    private var snackBar: Snackbar? = null
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get<MainScreenViewModel>(MainScreenViewModel::class.java)
        adapter = SearchResultsAdapter(this)
        main_locations_recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        main_locations_recycler.adapter = adapter

        savedInstanceState?.let {
            val query = it.getString(STATE_QUERY_STRING)
            query?.let { queryString ->
                viewModel.setQuery(queryString)
            }
        }

        main_swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        main_swipe_refresh.setOnRefreshListener {
            if (viewModel.searchFilter.value.isNullOrBlank()) {
                main_swipe_refresh.isRefreshing = false
                return@setOnRefreshListener
            }

            viewModel.refresh()
        }

        main_toggle_full_map.setOnClickListener {
            viewModel.locationResults.value?.let {
                FullMapActivity.launch(this, it)
            }
        }

        viewModel.locationResults.observe(this, Observer<ArrayList<LocationResult>> {
            //cancel the progress indicator
            main_swipe_refresh.isRefreshing = false

            //update adapter with new results
            adapter.setLocationResults(locations = it)
            setFullMapVisibleState()
        })

        viewModel.locationResultsError.observe(this, Observer<String> {
            if (it == null) {
                dismissSnackBar()
            } else {
                snackBar = Errors.showError(main_coordinator, R.string.request_failed_main, View.OnClickListener { viewModel.refresh() })
            }
        })
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
                    if (searchView.isIconified) {
                        view.onActionViewExpanded()
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

    /**
     * Handle clicks on the location view holders and startup the details screen
     */
    override fun onLocationClicked(id: String) {
        DetailsActivity.launch(this, id)
    }

    /**
     * Handle clicks on the favorite star to mark a location as a favorite or unfavorite them
     */
    override fun onFavoriteClicked(id: String) {
        Executors.newSingleThreadExecutor().submit {
            FavoritesDatabase.database(this).favoritesDao().getFavoriteById(id).let { fav ->
                if (fav == null) {
                    //add to database since its not in there
                    Executors.newSingleThreadExecutor().submit {
                        FavoritesDatabase.database(this).favoritesDao().addFavorite(favorite = Favorite(id))
                    }
                } else {
                    //remove from database
                    Executors.newSingleThreadExecutor().submit {
                        FavoritesDatabase.database(this).favoritesDao().removeFavoriteById(id)
                    }
                }
            }
        }
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
        viewModel.setQuery(query)
        searchView?.setQuery(query, true)

        runOnUiThread {
            if (query.isBlank()) {
                toggleEmptyState(true)
                viewModel.setQuery("")
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
                viewModel.setQuery("")
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
        viewModel.setQuery(query)
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
        if (viewModel.locationResults.value.isNullOrEmpty()) {
            //empty or blank query so lets hide the full map button
            main_toggle_full_map.hide()
        } else {
            //results exist or the search query is not blank so
            main_toggle_full_map.show()
        }
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
