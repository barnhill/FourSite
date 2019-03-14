package com.pnuema.android.codingchallenge.fullmap.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.details.ui.DetailsActivity
import com.pnuema.android.codingchallenge.helpers.MapUtils
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import com.pnuema.android.codingchallenge.fullmap.viewmodels.FullMapViewModel

/**
 * Full map screen
 */
class FullMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private lateinit var viewModel: FullMapViewModel
    private val mapMarkerToData: HashMap<Marker, LocationResult> = HashMap()

    companion object {
        const val PARAM_LOCATIONS: String = "PARAM_LOCATIONS"
        fun launch(context: Context, locations: ArrayList<LocationResult>) {
            val intent = Intent(context, FullMapActivity::class.java)
            intent.putParcelableArrayListExtra(PARAM_LOCATIONS, locations)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_map)

        if (!intent.hasExtra(PARAM_LOCATIONS)) {
            //launched without any locations so go back
            onBackPressed()
        }

        //setup view model to persist the locations so they survive rotation
        viewModel = ViewModelProviders.of(this).get<FullMapViewModel>(FullMapViewModel::class.java)
        viewModel.locationResults = intent.getParcelableArrayListExtra(PARAM_LOCATIONS)

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
        MapUtils.setPivotMarker(googleMap)
        addAllLocations(googleMap)
        googleMap.setOnInfoWindowClickListener(this)
    }

    /**
     * Handles title popup clicks to navigate to the details screen
     */
    override fun onInfoWindowClick(marker: Marker?) {
        marker?.let {
            val locationData = mapMarkerToData[marker]
            locationData?.id?.let {
                //navigate to the detail screen
                DetailsActivity.launch(this, it)
                return
            }

            //display an error message since we could not navigate to details due to location data being null
            Toast.makeText(this, R.string.error_location_data, Toast.LENGTH_LONG).show()
        }

        return
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
        latlngBuilder.include(MapUtils.pivotLatLong())

        //add padding as a percentage of the smallest width of the device
        val padding = (resources.configuration.smallestScreenWidthDp * 0.40).toInt()

        //animate the camera and zoom to the bounds of all the points (adjusted for padding) in the builder
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, padding))
    }
}
