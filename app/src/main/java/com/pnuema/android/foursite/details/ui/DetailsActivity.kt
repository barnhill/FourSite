package com.pnuema.android.foursite.details.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.pnuema.android.foursite.R
import com.pnuema.android.foursite.details.viewmodels.DetailsViewModel
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.content_details.*
import kotlin.math.roundToInt

/**
 * Activity for showing the detail screen
 */
class DetailsActivity : AppCompatActivity() {
    private val viewModel: DetailsViewModel by lazy { ViewModelProvider(fragment_details)[DetailsViewModel::class.java] }

    companion object {
        private const val PARAM_LOCATION: String = "PARAM_LOCATION"
        private const val PARAM_CURRENT_LOCATION: String = "PARAM_CURRENT_LOCATION"
        const val DETAILS_REQUEST_CODE = 204

        fun buildIntent(context: Context, locationId: String, currentLocation: LatLng): Intent {
            return Intent(context, DetailsActivity::class.java)
                        .putExtra(PARAM_LOCATION, locationId)
                        .putExtra(PARAM_CURRENT_LOCATION, currentLocation)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        viewModel.locationId = intent.getStringExtra(PARAM_LOCATION) ?: ""
        viewModel.currentLocation = intent.getParcelableExtra(PARAM_CURRENT_LOCATION)

        //set app bar size to half the measured screen so that the map gets the top half
        details_app_bar_layout.layoutParams.height = (resources.configuration.screenHeightDp * resources.displayMetrics.density).roundToInt() / 2
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
