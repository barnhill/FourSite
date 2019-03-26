package com.pnuema.android.codingchallenge.details.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pnuema.android.codingchallenge.api.FoursquareServiceProvider
import com.pnuema.android.codingchallenge.details.models.DetailsResponse
import com.pnuema.android.codingchallenge.details.models.VenueDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsViewModel : ViewModel() {
    var details: MutableLiveData<VenueDetail> = MutableLiveData()
    var locationId: String = ""

    /**
     * Asynchronously request the location details
     */
    fun refresh() {
        if (locationId.isBlank()) {
            return
        }

        FoursquareServiceProvider.service.getDetails(locationId).enqueue(object : Callback<DetailsResponse> {
            override fun onFailure(call: Call<DetailsResponse>, t: Throwable) {
                Log.e(javaClass.simpleName, t.message)
                details.postValue(null)
            }

            override fun onResponse(call: Call<DetailsResponse>, response: Response<DetailsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.response.let { detailResponse ->
                        detailResponse?.let {
                            it.venue?.let { venue ->
                                //successful response so parse the results and post them to the awaiting live data
                                details.postValue(venue)
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
