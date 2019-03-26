package com.pnuema.android.codingchallenge.mainscreen.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pnuema.android.codingchallenge.api.FoursquareServiceProvider
import com.pnuema.android.codingchallenge.mainscreen.models.FoursquareResponse
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainScreenViewModel : ViewModel() {
    var searchFilter: String = ""
    var locationResults: MutableLiveData<ArrayList<LocationResult>> = MutableLiveData()
    var locationResultsError: MutableLiveData<String> = MutableLiveData()

    private fun getLocationResults(query: String) {
        FoursquareServiceProvider.service.getLocationResults(query = query).enqueue(object :
            Callback<FoursquareResponse> {
            override fun onFailure(call: Call<FoursquareResponse>, t: Throwable) {
                Log.e(javaClass.simpleName, t.message)
                locationResultsError.postValue(t.message)
            }

            override fun onResponse(call: Call<FoursquareResponse>, response: Response<FoursquareResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    //successful response so parse the results and post them to the awaiting live data
                    response.body()?.let { foursquareResponse ->
                        locationResults.postValue(buildResults(foursquareResponse))
                    }
                } else {
                    onFailure(call, Throwable("Unsuccessful request for locations: " + response.code()))
                }
            }
        })
    }

    fun setQuery(query: String) {
        searchFilter = query

        if (query.isBlank()) {
            locationResults.postValue(ArrayList())
            return
        } else {
            getLocationResults(searchFilter)
        }
    }

    fun refresh() {
        getLocationResults(searchFilter)
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

            return java.util.ArrayList(resultsList.sortedBy { it.locationDistance })
        }

        return java.util.ArrayList()
    }
}