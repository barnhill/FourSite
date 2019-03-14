package com.pnuema.android.codingchallenge.details.requests

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.pnuema.android.codingchallenge.details.models.DetailsResponse
import com.pnuema.android.codingchallenge.details.models.VenueDetail
import com.pnuema.android.codingchallenge.api.FoursquareServiceProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsRequest {
    companion object {
        /**
         * Asynchronously request the location details
         *
         * @param locationId The location id of the venue to get the details
         */
        fun getLocationDetails(locationId: String, liveResponse: MutableLiveData<VenueDetail>) {
            FoursquareServiceProvider.service.getDetails(locationId).enqueue(object : Callback<DetailsResponse> {
                override fun onFailure(call: Call<DetailsResponse>, t: Throwable) {
                    Log.e("DetailsRequest", t.message)
                    liveResponse.postValue(null)
                }

                override fun onResponse(call: Call<DetailsResponse>, response: Response<DetailsResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.response.let { detailResponse ->
                            detailResponse?.let {
                                it.venue?.let { venue ->
                                    //successful response so parse the results and post them to the awaiting live data
                                    liveResponse.postValue(venue)
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
    }
}