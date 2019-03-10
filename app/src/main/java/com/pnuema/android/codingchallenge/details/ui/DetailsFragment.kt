package com.pnuema.android.codingchallenge.details.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.details.models.VenueDetail
import com.pnuema.android.codingchallenge.details.requests.DetailsRequest
import com.pnuema.android.codingchallenge.details.viewmodels.DetailsViewModel
import com.pnuema.android.codingchallenge.helpers.MapUtils
import com.pnuema.android.codingchallenge.persistance.FavoritesDatabase
import kotlinx.android.synthetic.main.fragment_details.*
import java.text.DecimalFormat
import java.util.concurrent.Executors

/**
 * Fragment to display location details
 */
class DetailsFragment : Fragment() {
    companion object {
        private const val PERMISSION_CALL_REQUEST_CODE = 124
    }
    private lateinit var viewModel: DetailsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(DetailsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val locationId = viewModel.locationId
        if (locationId == null) {
            activity?.onBackPressed()
            return
        }

        //request the detail information from the API
        val liveResponse: MutableLiveData<VenueDetail> = MutableLiveData()
        liveResponse.observe(this, Observer { venueDetail ->
            populateScreen(venueDetail)
        })
        DetailsRequest.getLocationDetails(locationId, liveResponse)
    }

    /**
     * Populates the screen with the data retrieved from the API
     */
    private fun populateScreen(venueDetail: VenueDetail?) {
        if (venueDetail == null || !isAdded || context == null) {
            activity?.onBackPressed()
        }

        val details = venueDetail!!

        viewModel.details = details

        //populate the screen with details
        details_name.text = details.name

        if (details.ratingSignals != null) {
            details_reviews.text = getString(R.string.details_review_count_format, details.ratingSignals)
        } else {
            details_reviews.text = getString(R.string.details_review_count_format, 0)
        }

        //ratings
        details_rating.text = "0.0"
        details_rating_bar.rating = 0.0f
        details.rating?.let { ratingScore ->
            details_rating.text = DecimalFormat("#.##").format(ratingScore.div(2))
            details_rating_bar.rating = (ratingScore.div(2)).toFloat()

            //set the ratings bar to the color provided if its available
            details.ratingColor?.let { ratingColor ->
                DrawableCompat.setTint(details_rating_bar.progressDrawable, Color.parseColor("#$ratingColor"))
            }
        }

        //hours
        details_hours.text = details.hours?.status

        //address fields
        details.location?.formattedAddress.let {
            var addressString = ""
            it?.forEach { s ->
                addressString += s + System.getProperty("line.separator")
            }

            details_address.text = addressString
        }

        //category
        details.categories?.let {
            details_category.text = if (it.isEmpty()) "" else it[0].shortName
        }

        //website button
        if (details.canonicalUrl.isNullOrBlank() && details.shortUrl.isNullOrBlank()) {
            details_web.visibility = View.GONE
        } else {
            if (details.canonicalUrl.isNullOrBlank()) {
                details_web.text = details.shortUrl
            } else {
                details_web.text = details.canonicalUrl
            }
            details_web.visibility = View.VISIBLE
            details_web.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(details_web.text.toString())))
            }
        }

        //phone button
        val contact = details.contact
        val number = details.contact?.phone
        if (contact == null || contact.formattedPhone.isNullOrBlank() || number.isNullOrBlank()) {
            details_phone.visibility = View.GONE
        } else {
            details_phone.visibility = View.VISIBLE
            details_phone.text = contact.formattedPhone

            details_phone.setOnClickListener {
                handleCall(number)
            }
        }

        //show or hide the favorites based on if this location has been favorited by the user
        Executors.newSingleThreadExecutor().submit {
            details.id?.let {locationId ->
                context?.let { context ->
                    val isFavorite = FavoritesDatabase.database(context).favoritesDao().getFavoriteById(locationId).id == locationId
                    details_is_favorite.visibility = if (isFavorite) View.VISIBLE else View.GONE
                }
            }
        }

        (fragmentManager?.findFragmentById(R.id.details_map) as SupportMapFragment).getMapAsync { googleMap ->
            val latlngBuilder = LatLngBounds.Builder()

            googleMap.uiSettings.isScrollGesturesEnabled = false
            googleMap.uiSettings.isZoomGesturesEnabled = false
            googleMap.setOnMarkerClickListener { true } //disable click on pins

            //pin the pivot location
            MapUtils.setPivotMarker(googleMap)
            latlngBuilder.include(MapUtils.pivotLatLong())

            //if all fields exist pin the detail location
            details.location?.lat?.let { lat ->
                details.location?.lng?.let { lng ->
                    latlngBuilder.include(LatLng(lat, lng))
                    MapUtils.setMarker(googleMap, lat, lng, details.name)
                }
            }

            //move camera to the detail location without animation, add padding to sides of map area
            val padding = (resources.configuration.smallestScreenWidthDp * 0.40).toInt()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), padding))
        }
    }

    private fun handleCall(number: String) {
        if (ContextCompat.checkSelfPermission(activity as Context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity as Activity, arrayOf( Manifest.permission.CALL_PHONE), PERMISSION_CALL_REQUEST_CODE)
            return
        }

        startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CALL_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //permission denied by user
                    details_phone.isEnabled = false
                } else {
                    //permission granted
                    val number = viewModel.details?.contact?.phone
                    if (!number.isNullOrBlank()) {
                        handleCall(number)
                    }
                }
            }
        }
    }
}
