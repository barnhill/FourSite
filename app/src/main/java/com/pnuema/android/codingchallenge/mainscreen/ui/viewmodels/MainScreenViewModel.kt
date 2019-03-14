package com.pnuema.android.codingchallenge.mainscreen.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult

class MainScreenViewModel : ViewModel() {
    var searchFilter: String = ""
    var locationResults: ArrayList<LocationResult> = ArrayList()
}