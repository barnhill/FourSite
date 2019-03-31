package com.pnuema.android.foursite.fullmap.viewmodels

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.pnuema.android.foursite.mainscreen.ui.models.LocationResult

class FullMapViewModel : ViewModel() {
    var locationResults: List<LocationResult> = ArrayList()
    var currentLocation: LatLng? = null
}