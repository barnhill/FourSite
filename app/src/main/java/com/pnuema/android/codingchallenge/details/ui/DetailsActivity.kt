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
    private lateinit var viewModel: DetailsViewModel

    companion object {
        private const val PARAM_LOCATION: String = "PARAM_LOCATION"

        fun launch(context: Context, locationId: String) {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra(PARAM_LOCATION, locationId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(toolbar)

        title = ""
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }

        viewModel = ViewModelProviders.of(fragment_details).get(DetailsViewModel::class.java)
        viewModel.locationId = intent.getStringExtra(PARAM_LOCATION)

        //set app bar size to half the measured screen so that the map gets the top half
        details_app_bar_layout.layoutParams.height = Math.round(resources.configuration.screenHeightDp * resources.displayMetrics.density) / 2
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
