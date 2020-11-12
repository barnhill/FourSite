package com.pnuema.android.foursite.mainscreen.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.pnuema.android.foursite.R
import com.pnuema.android.foursite.details.ui.DetailsActivity
import com.pnuema.android.foursite.fullmap.ui.FullMapActivity
import com.pnuema.android.foursite.helpers.Errors
import com.pnuema.android.foursite.mainscreen.ui.adapters.SearchResultsAdapter
import com.pnuema.android.foursite.mainscreen.ui.models.LocationResult
import com.pnuema.android.foursite.mainscreen.ui.viewholders.LocationClickListener
import com.pnuema.android.foursite.mainscreen.ui.viewmodels.MainScreenViewModel
import com.pnuema.android.foursite.persistance.FavoritesDatabase
import com.pnuema.android.foursite.persistance.daos.Favorite
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
        private const val PERMISSION_LOCATION_REQUEST_CODE = 579
    }
    private val viewModel: MainScreenViewModel by lazy { ViewModelProvider(this)[MainScreenViewModel::class.java] }
    private val adapter: SearchResultsAdapter by lazy { SearchResultsAdapter(this) }
    private val locationProviderClient: FusedLocationProviderClient by lazy { FusedLocationProviderClient(this) }
    private var snackBar: Snackbar? = null
    private var searchView: SearchView? = null
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        handleGetLocation()

        main_locations_recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        main_locations_recycler.adapter = adapter

        savedInstanceState?.let {
            val query = it.getString(STATE_QUERY_STRING)
            query?.let { queryString ->
                viewModel.setQuery(queryString, currentLocation)
            }
        }

        main_swipe_refresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        main_swipe_refresh.setOnRefreshListener {
            if (viewModel.searchFilter.isBlank()) {
                main_swipe_refresh.isRefreshing = false
                return@setOnRefreshListener
            }

            dismissSnackBar()

            if (currentLocation == null) {
                handleGetLocation()
            } else {
                viewModel.refresh(currentLocation)
            }
        }

        main_toggle_full_map.setOnClickListener {
            viewModel.locationResults.value?.let { locations ->
                currentLocation?.let { latLng ->
                    startActivityForResult(FullMapActivity.buildIntent(this, locations, LatLng(latLng.latitude, latLng.longitude)), FullMapActivity.FULLMAP_REQUEST_CODE)
                }
            }
        }

        viewModel.locationResults.observe(this, Observer<ArrayList<LocationResult>> {
            //cancel the progress indicator
            main_swipe_refresh.isRefreshing = false
            dismissSnackBar()

            if (it.isEmpty()) {
                //set empty state
                toggleEmptyState(true)
            } else {
                //update adapter with new results
                toggleEmptyState(false)
                adapter.setLocationResults(locations = it)

                viewModel.checkLocationResultsFavorites(this, it, object: MainScreenViewModel.LocationFavoriteChanged{
                    override fun onFavoriteChangedStatus() {
                        runOnUiThread {
                            adapter.setLocationResults(it)
                        }
                    }
                })
            }

            setFullMapVisibleState(it)
        })

        viewModel.locationResultsError.observe(this, Observer<Int> {
            dismissSnackBar()
            if (it != null) {
                when (it) {
                    MainScreenViewModel.ERROR_CODE_RETRIEVE -> snackBar = Errors.showError(main_coordinator, R.string.request_failed_main, R.string.retry, View.OnClickListener {
                        dismissSnackBar()
                        viewModel.refresh(currentLocation)
                    })
                    MainScreenViewModel.ERROR_CODE_NO_CURRENT_LOCATION -> snackBar = Errors.showError(main_coordinator, R.string.error_location_permission_denied, R.string.enable, View.OnClickListener {
                        //launch app detail settings page to let the user enable the permission that they denied
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                        finish()
                    })
                }
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
                    view.queryHint = getString(R.string.search_locations)

                    //watch for more user input before firing off the api requests to cut down on traffic
                    setupQueryInputWatcher(view)
                }
            }
        }

        return true
    }

    @SuppressLint("CheckResult")
    private fun setupQueryInputWatcher(searchView: SearchView) {
        Observable.create(ObservableOnSubscribe<String> { subscriber ->
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        adapter.notifyDataSetChanged()
    }

    /**
     * Handle clicks on the location view holders and startup the details screen
     */
    override fun onLocationClicked(id: String) {
        currentLocation?.let { currentLocation ->
            startActivityForResult(DetailsActivity.buildIntent(this, id, LatLng(currentLocation.latitude, currentLocation.longitude)), DetailsActivity.DETAILS_REQUEST_CODE)
        }
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
        searchView?.setQuery(query, true)

        runOnUiThread {
            if (query.isBlank()) {
                viewModel.setQuery("", currentLocation)
            } else {
                main_swipe_refresh.isRefreshing = true
                dismissSnackBar()
                viewModel.setQuery(query, currentLocation)
            }

            if (currentLocation == null) {
                dismissSnackBar()
                handleGetLocation()
            }
        }
    }

    /**
     * Handle standard on back pressed events but intercept back presses
     * and clear out the search results first before exiting on a subsequent call
     */
    override fun onBackPressed() {
        searchView?.let {
            if (!it.isIconified) {
                it.onActionViewCollapsed()
                return
            }
        }
        super.onBackPressed()
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
    private fun setFullMapVisibleState(locationResults: ArrayList<LocationResult>) {
        if (locationResults.isEmpty()) {
            //empty or blank query so lets hide the full map button
            main_toggle_full_map.animate().translationY((main_toggle_full_map.height + main_toggle_full_map.marginBottom).toFloat()).setInterpolator(AccelerateDecelerateInterpolator()).start()
            main_toggle_full_map.hide()
        } else {
            //results exist or the search query is not blank so
            main_toggle_full_map.show()
            main_toggle_full_map.animate().translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).start()
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

    private fun handleGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_LOCATION_REQUEST_CODE
            )
            return
        }

        locationProviderClient.lastLocation.addOnSuccessListener {
            currentLocation = it
            dismissSnackBar()
            viewModel.refresh(currentLocation)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //permission denied by user
                    dismissSnackBar()
                    AlertDialog.Builder(this)
                        .setMessage(R.string.error_closing_no_location_permission)
                        .setPositiveButton(R.string.close) { _, _ -> finish() }
                        .setNegativeButton(R.string.enable) {_, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", packageName, null)
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                } else {
                    //permission granted
                    dismissSnackBar()
                    handleGetLocation()
                }
            }
        }
    }
}
