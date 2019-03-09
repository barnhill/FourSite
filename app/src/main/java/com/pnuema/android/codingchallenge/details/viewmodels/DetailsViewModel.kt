package com.pnuema.android.codingchallenge.details.viewmodels

import androidx.lifecycle.ViewModel
import com.pnuema.android.codingchallenge.details.models.VenueDetail

class DetailsViewModel : ViewModel() {
    var details: VenueDetail? = null
    var locationId: String? = null
}
