package com.pnuema.android.foursite.fullmap.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.pnuema.android.foursite.R
import com.pnuema.android.foursite.details.ui.DetailsActivity
import com.pnuema.android.foursite.fullmap.viewmodels.FullMapViewModel
import com.pnuema.android.foursite.helpers.MapUtils
import com.pnuema.android.foursite.mainscreen.ui.models.LocationResult

/**
 * Full map screen
 */
class FullMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private val viewModel: FullMapViewModel by lazy { ViewModelProvider(this)[FullMapViewModel::class.java] }
    private val mapMarkerToData: HashMap<Marker, LocationResult> = HashMap()

    companion object {
        const val PARAM_LOCATIONS: String = "PARAM_LOCATIONS"
        private const val PARAM_CURRENT_LOCATION: String = "PARAM_CURRENT_LOCATION"
        const val FULLMAP_REQUEST_CODE = 205

        fun buildIntent(context: Context, locations: ArrayList<LocationResult>, currentLatLng: LatLng): Intent {
            return Intent(context, FullMapActivity::class.java)
                        .putParcelableArrayListExtra(PARAM_LOCATIONS, locations)
                        .putExtra(PARAM_CURRENT_LOCATION, currentLatLng)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_map)

        if (!intent.hasExtra(PARAM_LOCATIONS)) {
            //launched without any locations so go back
            onBackPressed()
        }

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        //setup view model to persist the locations so they survive rotation
        viewModel.locationResults = intent.getParcelableArrayListExtra(PARAM_LOCATIONS)!!
        viewModel.currentLocation = intent.getParcelableExtra(PARAM_CURRENT_LOCATION)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        viewModel.currentLocation?.let { latlng ->
            MapUtils.setPivotMarker(getString(R.string.current_location_label), googleMap, latlng.latitude, latlng.longitude)
            addAllLocations(googleMap)
            googleMap.setOnInfoWindowClickListener(this)
        }
    }

    /**
     * Handles title popup clicks to navigate to the details screen
     */
    override fun onInfoWindowClick(marker: Marker?) {
        marker?.let {
            val locationData = mapMarkerToData[marker]
            locationData?.id?.let { locationId ->
                //navigate to the detail screen
                viewModel.currentLocation?.let {latLng: LatLng ->
                    startActivity(DetailsActivity.buildIntent(this, locationId, latLng))
                }
                return
            }

            //display an error message since we could not navigate to details due to location data being null
            Toast.makeText(this, R.string.error_location_data, Toast.LENGTH_LONG).show()
        }

        return
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Add all points to the map to graph, also add all points to the bounds so the zoom window can be calculated
     */
    private fun addAllLocations(googleMap: GoogleMap) {
        val latlngBuilder = LatLngBounds.Builder()
        viewModel.locationResults.forEach {
            if (it.lat != null && it.lng != null) {
                val marker = MapUtils.setMarker(googleMap, it.lat, it.lng, it.locationName)
                latlngBuilder.include(LatLng(it.lat, it.lng))

                mapMarkerToData[marker] = it
            }
        }

        //include the center point in the calculation for the bounds
        latlngBuilder.include(viewModel.currentLocation)

        //add padding as a percentage of the smallest width of the device
        val padding = (resources.configuration.smallestScreenWidthDp * 0.40).toInt()

        //animate the camera and zoom to the bounds of all the points (adjusted for padding) in the builder
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, padding))
    }
}
