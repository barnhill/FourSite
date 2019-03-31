package com.pnuema.android.foursite.api

import com.pnuema.android.foursite.details.models.DetailsResponse
import com.pnuema.android.foursite.mainscreen.models.FoursquareResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Service endpoints for interacting with the Foursquare API
 */
interface FoursquareService {
    companion object {
        private const val CLIENT_ID = "IK4QB0KWMKJMNIFDXJY5X51VUP4NPXSPV0K12D4Z3D5YZUQZ"
        private const val CLIENT_SECRET = "PGSIDON1C2TSKTSDNKIK4G2GSMCRMK1AXTKUJZWUU0GRWWEL"
        private const val VERSION = "20180401"
        private const val COMMON_PARAMS = "&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&v=$VERSION"

        //47.606200,-122.332100     ll=47.606200,-122.332100&
    }

    @GET("/v2/venues/search?limit=20$COMMON_PARAMS")
    fun getLocationResults(@Query("query") query: String, @Query("ll") latlng: String): Call<FoursquareResponse>

    @GET("/v2/venues/{venue_id}/?$COMMON_PARAMS")
    fun getDetails(@Path("venue_id") venueId: String): Call<DetailsResponse>
}