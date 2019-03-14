package com.pnuema.android.codingchallenge.mainscreen.ui.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.details.ui.DetailsActivity
import com.pnuema.android.codingchallenge.glide.GlideApp
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import com.pnuema.android.codingchallenge.persistance.FavoritesDatabase
import com.pnuema.android.codingchallenge.persistance.daos.Favorite
import kotlinx.android.synthetic.main.location_result_item.view.*
import java.util.concurrent.Executors

/**
 * View holder for display of the location information on the main screen of the app in a list.
 * Allows favoriting, and navigation to the detail screen
 */
class LocationResultViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_result_item, parent, false)) {
    fun bind(locationResult: LocationResult) {
        val context = itemView.context

        //name and category display
        itemView.locationName.text = locationResult.locationName?: ""
        itemView.locationCategory.text = locationResult.locationCategory?: ""

        //distance display
        itemView.locationDistance.text = ""
        locationResult.locationDistance?.let {
            itemView.locationDistance.text = context.resources.getQuantityString(R.plurals.meters, it.toInt(), it.toInt())
        }

        //load the image async with Glide so that the UI doesnt have to wait around on images to load (GlideConfig.kt)
        GlideApp.with(context).load(locationResult.locationIcon).into(itemView.locationImage)

        //set the initial state of the favorites icon by checking if its a favorite in the database
        setupFavoriteIndicator(locationResult)

        //launch the details screen on click
        itemView.setOnClickListener {
            locationResult.id?.let {id ->
                DetailsActivity.launch(context, id)
            }
        }
    }

    private fun setupFavoriteIndicator(locationResult: LocationResult) {
        val context = itemView.context

        itemView.locationFavorite.isChecked = false //set default

        locationResult.id?.let {
            //set the views state based on what is in the database
            Executors.newSingleThreadExecutor().submit {
                FavoritesDatabase.database(context).favoritesDao().getFavoriteById(it)?.let { fav ->
                    itemView.locationFavorite.isChecked = fav.id == it
                }
            }
        }

        //handle the status changes for favorites when the user clicks the star
        itemView.locationFavorite.setOnClickListener {
            locationResult.id?.let {
                if (itemView.locationFavorite.isChecked) {
                    //add to database if not already in there
                    Executors.newSingleThreadExecutor().submit {
                        FavoritesDatabase.database(context).favoritesDao().addFavorite(favorite = Favorite(locationResult.id))
                    }
                } else {
                    //remove from database
                    Executors.newSingleThreadExecutor().submit {
                        FavoritesDatabase.database(context).favoritesDao().removeFavoriteById(locationResult.id)
                    }
                }
            }
        }
    }
}