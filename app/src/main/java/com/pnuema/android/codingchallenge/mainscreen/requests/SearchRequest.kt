package com.pnuema.android.codingchallenge.mainscreen.requests

import android.util.Log
import com.pnuema.android.codingchallenge.mainscreen.models.FoursquareResponse
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import com.pnuema.android.codingchallenge.api.FoursquareServiceProvider
import com.pnuema.android.codingchallenge.api.LocationResultsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchRequest {
    /**
     * Asynchronously request the location results
     *
     * @param query The search term to look for in the api
     */
    fun getLocationResults(query: String, locationResultsListener: LocationResultsListener) {
        FoursquareServiceProvider.service.getLocationResults(query = query).enqueue(object : Callback<FoursquareResponse> {
            override fun onFailure(call: Call<FoursquareResponse>, t: Throwable) {
                Log.e("SearchRequest", t.message)
                locationResultsListener.failed()
            }

            override fun onResponse(call: Call<FoursquareResponse>, response: Response<FoursquareResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    //successful response so parse the results and post them to the awaiting live data
                    response.body()?.let { foursquareResponse ->
                        locationResultsListener.success(buildResults(foursquareResponse))
                    }
                } else {
                    onFailure(call, Throwable("Unsuccessful request for locations: " + response.code()))
                }
            }
        })
    }

    /**
     * Build the list of location results sorted by distance
     *
     * @return empty list of no results were found in the response
     */
    private fun buildResults(foursquareResponse: FoursquareResponse): ArrayList<LocationResult> {
        foursquareResponse.response?.let { response ->
            val resultsList = ArrayList<LocationResult>()
            response.venues?.forEach { resultsList.add(LocationResult(it)) }

            return ArrayList(resultsList.sortedBy { it.locationDistance })
        }

        return ArrayList()
    }
}