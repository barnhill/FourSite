package com.pnuema.android.codingchallenge.api

import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult

interface LocationResultsListener {
    fun success(locations: ArrayList<LocationResult>)
    fun failed()
}