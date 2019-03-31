package com.pnuema.android.foursite.mainscreen.ui.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pnuema.android.foursite.R
import com.pnuema.android.foursite.glide.GlideApp
import com.pnuema.android.foursite.mainscreen.ui.models.LocationResult
import com.pnuema.android.foursite.persistance.FavoritesDatabase
import kotlinx.android.synthetic.main.location_result_item.view.*
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/**
 * View holder for display of the location information on the main screen of the app in a list.
 * Allows favoriting, and navigation to the detail screen
 */
class LocationResultViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_result_item, parent, false)) {
    fun bind(locationResult: LocationResult, onClickListener: LocationClickListener) {
        val context = itemView.context

        //name and category display
        itemView.locationName.text = locationResult.locationName?: ""
        itemView.locationCategory.text = locationResult.locationCategory?: ""

        //distance display
        itemView.locationDistance.text = ""
        locationResult.locationDistance?.let {meters ->
            //if longer than a mile display miles
            if (meters >= 1609.34) {
                val miles = (meters / 1609.34) //convert to miles
                itemView.locationDistance.text = context.resources.getQuantityString(R.plurals.miles, miles.roundToInt(), String.format("%.1f", miles))
            } else {
                val feet = (meters / 3.28084).roundToInt() //convert to feet
                itemView.locationDistance.text = context.resources.getQuantityString(R.plurals.feet, feet, feet.toString())
            }
        }

        //load the image async with Glide so that the UI doesnt have to wait around on images to load (GlideConfig.kt)
        GlideApp.with(context).load(locationResult.locationIcon).into(itemView.locationImage)

        //set the initial state of the favorites icon by checking if its a favorite in the database
        setupFavoriteIndicator(locationResult, onClickListener)

        //send the click event to the listener
        itemView.setOnClickListener{
            locationResult.id?.let {
                onClickListener.onLocationClicked(it)
            }
        }
    }

    private fun setupFavoriteIndicator(locationResult: LocationResult, clickListener: LocationClickListener) {
        itemView.locationFavorite.isChecked = false //set default

        locationResult.id?.let {locationId ->
            //set the views state based on what is in the database
            Executors.newSingleThreadExecutor().submit {
                FavoritesDatabase.database(itemView.context).favoritesDao().getFavoriteById(locationId)?.let { fav ->
                    itemView.locationFavorite.isChecked = fav.id == locationId
                }
            }
        }

        //handle the status changes for favorites when the user clicks the star
        itemView.locationFavorite.setOnClickListener {
            locationResult.id?.let {
                clickListener.onFavoriteClicked(it)
            }
        }
    }
}