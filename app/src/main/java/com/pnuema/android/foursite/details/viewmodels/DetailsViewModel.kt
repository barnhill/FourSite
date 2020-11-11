package com.pnuema.android.foursite.details.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.pnuema.android.foursite.api.FoursquareServiceProvider
import com.pnuema.android.foursite.details.models.DetailsResponse
import com.pnuema.android.foursite.details.models.VenueDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class DetailsViewModel : ViewModel() {
    var details: MutableLiveData<VenueDetail> = MutableLiveData()
    var locationId: String = ""
    var currentLocation: LatLng? = null

    var venueName: MutableLiveData<String> = MutableLiveData()
    var venueRating: MutableLiveData<String> = MutableLiveData()
    var venueBar: MutableLiveData<Float> = MutableLiveData()
    var venueReviews: MutableLiveData<Int> = MutableLiveData()
    //var venueBarColor: MutableLiveData<Int> = MutableLiveData()
    var venueHours: MutableLiveData<String> = MutableLiveData()
    var venueAddress: MutableLiveData<String> = MutableLiveData()
    var venueCategory: MutableLiveData<String> = MutableLiveData()
    var venueWebsite: MutableLiveData<String> = MutableLiveData()
    var venuePhone: MutableLiveData<String> = MutableLiveData()

    var error: MutableLiveData<String> = MutableLiveData()

    /**
     * Asynchronously request the location details
     */
    fun refresh() {
        if (locationId.isBlank()) {
            return
        }

        FoursquareServiceProvider.service.getDetails(locationId).enqueue(object : Callback<DetailsResponse> {
            override fun onFailure(call: Call<DetailsResponse>, t: Throwable) {
                Log.e(javaClass.simpleName, t.message ?: "")
                error.postValue(t.message)
            }

            override fun onResponse(call: Call<DetailsResponse>, response: Response<DetailsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.response.let { detailResponse ->
                        detailResponse?.let {
                            it.venue?.let { venue ->
                                //successful response so parse the results and post them to the awaiting live data
                                details.postValue(venue)

                                //post to live datas bound fields
                                venueName.value = venue.name
                                venueRating.value = formatRatings(venue)
                                venueBar.value = ratingBar(venue)
                                //venueBarColor.postValue(ratingBarColor(venue.ratingColor))
                                venueReviews.value = venue.ratingSignals
                                venueHours.value = venue.hours?.status?:""
                                venueAddress.value = address(venue)
                                venueCategory.value = category(venue)
                                venueWebsite.value = website(venue)
                                venuePhone.value = phone(venue)

                                return
                            }
                        }
                    }
                    onFailure(call, Throwable("Invalid data for location details"))
                } else {
                    onFailure(call, Throwable("Unsuccessful request for location details: " + response.code()))
                }
            }
        })
    }

    private fun formatRatings(venueDetail: VenueDetail): String {
        return DecimalFormat("#.##").format(venueDetail.rating?.div(2)?:0) ?: "0"
    }

    private fun ratingBar(venueDetail: VenueDetail): Float {
        return venueDetail.rating?.div(2)?.toFloat()?:0f
    }

    /*private fun ratingBarColor(ratingColor: String?): Int {
        return Color.parseColor("#$ratingColor")
    }*/

    private fun address(venueDetail: VenueDetail) : String {
        if (venueDetail.location == null || venueDetail.location?.formattedAddress.isNullOrEmpty()) {
            return ""
        }

        var addressString = ""
        venueDetail.location?.formattedAddress.let {
            it?.forEach { s ->
                addressString += s + System.getProperty("line.separator")
            }
        }

        return addressString
    }

    private fun category(venueDetail: VenueDetail) : String {
        return venueDetail.categories?.get(0)?.shortName?:""
    }

    private fun website(venueDetail: VenueDetail) : String {
        return if (venueDetail.canonicalUrl.isNullOrBlank()) {
            venueDetail.shortUrl?:""
        } else {
            venueDetail.canonicalUrl?:""
        }
    }

    private fun phone(venueDetail: VenueDetail) : String {
        val contact = venueDetail.contact
        return contact?.formattedPhone?:""
    }

    fun websiteVisibility(website: String?) : Int {
       return if (website.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
    }

    fun phoneVisibility(phone: String?) : Int {
        return if (phone.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
    }
}
