package com.pnuema.android.codingchallenge.fullmap.viewmodels

import androidx.lifecycle.ViewModel
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult

class FullMapViewModel : ViewModel() {
    var locationResults: List<LocationResult> = ArrayList()
}