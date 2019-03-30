package com.pnuema.android.codingchallenge.details.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.details.viewmodels.DetailsViewModel
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.content_details.*

/**
 * Activity for showing the detail screen
 */
class DetailsActivity : AppCompatActivity() {
    private val viewModel: DetailsViewModel by lazy { ViewModelProviders.of(fragment_details).get(DetailsViewModel::class.java) }

    companion object {
        private const val PARAM_LOCATION: String = "PARAM_LOCATION"
        const val DETAILS_REQUEST_CODE = 204

        fun buildIntent(context: Context, locationId: String): Intent {
            return Intent(context, DetailsActivity::class.java)
                        .putExtra(PARAM_LOCATION, locationId)
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

        viewModel.locationId = intent.getStringExtra(PARAM_LOCATION)

        //set app bar size to half the measured screen so that the map gets the top half
        details_app_bar_layout.layoutParams.height = Math.round(resources.configuration.screenHeightDp * resources.displayMetrics.density) / 2
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
