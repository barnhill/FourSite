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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.snackbar.Snackbar
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.details.models.VenueDetail
import com.pnuema.android.codingchallenge.details.viewmodels.DetailsViewModel
import com.pnuema.android.codingchallenge.helpers.Errors
import com.pnuema.android.codingchallenge.helpers.MapUtils
import com.pnuema.android.codingchallenge.persistance.FavoritesDatabase
import com.pnuema.android.codingchallenge.persistance.daos.Favorite
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

    private var snackBar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(DetailsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //handle bad data by going back if no location id
        val locationId = viewModel.locationId
        if (locationId.isBlank()) {
            activity?.onBackPressed()
            return
        }

        dismissSnackBar()
        viewModel.details.observe(viewLifecycleOwner, Observer { venueDetail ->
            if (venueDetail == null) {
                //no data returned which is indicative of an error case, so show an error message
                activity?.let {
                    activity?.findViewById<CoordinatorLayout>(R.id.details_coordinator)?.let {
                        snackBar = Errors.showError(it, R.string.request_failed_details, View.OnClickListener {
                            dismissSnackBar()
                            viewModel.refresh()
                        })
                    }
                }
            } else {
                //data returned successfully so lets populate the screen
                populateScreen(venueDetail)
            }
        })
        viewModel.refresh()
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
     * Populates the screen with the data retrieved from the API
     */
    private fun populateScreen(venueDetail: VenueDetail?) {
        if (venueDetail == null || !isAdded || context == null) {
            activity?.onBackPressed()
            return
        }

        //populate the screen with details
        details_name.text = venueDetail.name

        if (venueDetail.ratingSignals != null) {
            details_reviews.text = getString(R.string.details_review_count_format, venueDetail.ratingSignals)
        } else {
            details_reviews.text = getString(R.string.details_review_count_format, 0)
        }

        //ratings
        details_rating.text = "0.0"
        details_rating_bar.rating = 0.0f
        details_rating_bar.visibility = View.VISIBLE
        venueDetail.rating?.let { ratingScore ->
            details_rating.text = DecimalFormat("#.##").format(ratingScore.div(2))
            details_rating_bar.rating = (ratingScore.div(2)).toFloat()

            //set the ratings bar to the color provided if its available
            venueDetail.ratingColor?.let { ratingColor ->
                DrawableCompat.setTint(details_rating_bar.progressDrawable, Color.parseColor("#$ratingColor"))
            }
        }

        //hours
        details_hours.text = venueDetail.hours?.status

        //address fields
        venueDetail.location?.formattedAddress.let {
            var addressString = ""
            it?.forEach { s ->
                addressString += s + System.getProperty("line.separator")
            }

            details_address.text = addressString
        }

        //category
        venueDetail.categories?.let {
            details_category.text = if (it.isEmpty()) "" else it[0].shortName
        }

        //website button
        if (venueDetail.canonicalUrl.isNullOrBlank() && venueDetail.shortUrl.isNullOrBlank()) {
            details_web.visibility = View.GONE
        } else {
            if (venueDetail.canonicalUrl.isNullOrBlank()) {
                details_web.text = venueDetail.shortUrl
            } else {
                details_web.text = venueDetail.canonicalUrl
            }
            details_web.visibility = View.VISIBLE
            details_web.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(details_web.text.toString())))
            }
        }

        //phone button
        val contact = venueDetail.contact
        val number = venueDetail.contact?.phone
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
            venueDetail.id?.let { locationId ->
                context?.let { context ->
                    val favorite = FavoritesDatabase.database(context).favoritesDao().getFavoriteById(locationId)
                    val isFavorite = favorite != null && favorite.id == locationId
                    details_is_favorite.setImageDrawable(ContextCompat.getDrawable(context, if (isFavorite) R.drawable.star_circle else R.drawable.star_circle_disabled))
                }
            }
        }

        //add click listener for adding/removing favorite locale
        details_is_favorite.setOnClickListener {
            Executors.newSingleThreadExecutor().submit {
                venueDetail.id?.let { locationId ->
                    context?.let { context ->
                        val favorite = FavoritesDatabase.database(context).favoritesDao().getFavoriteById(locationId)
                        val isFavorite = favorite != null && favorite.id == locationId

                        if (isFavorite) {
                            FavoritesDatabase.database(context).favoritesDao().removeFavoriteById(locationId)
                            activity?.runOnUiThread {
                                view?.let {
                                    Snackbar.make(it, getString(R.string.removed_favorite), Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            FavoritesDatabase.database(context).favoritesDao().addFavorite(Favorite(locationId))
                            activity?.runOnUiThread {
                                view?.let {
                                    Snackbar.make(it, getString(R.string.added_favorite), Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }

                        details_is_favorite.setImageDrawable(
                            ContextCompat.getDrawable(context, if (!isFavorite) R.drawable.star_circle else R.drawable.star_circle_disabled)
                        )
                    }
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
            venueDetail.location?.lat?.let { lat ->
                venueDetail.location?.lng?.let { lng ->
                    latlngBuilder.include(LatLng(lat, lng))
                    MapUtils.setMarker(googleMap, lat, lng, venueDetail.name)
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
                    val number = viewModel.details.value?.contact?.phone
                    if (!number.isNullOrBlank()) {
                        handleCall(number)
                    }
                }
            }
        }
    }
}
