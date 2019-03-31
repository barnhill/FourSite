package com.pnuema.android.foursite.api

import com.pnuema.android.foursite.mainscreen.ui.models.LocationResult

interface LocationResultsListener {
    fun success(locations: ArrayList<LocationResult>)
    fun failed()
}