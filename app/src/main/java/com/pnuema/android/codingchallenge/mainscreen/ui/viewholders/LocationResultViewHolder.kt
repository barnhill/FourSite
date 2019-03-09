package com.pnuema.android.codingchallenge.mainscreen.ui.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pnuema.android.codingchallenge.R
import com.pnuema.android.codingchallenge.glide.GlideApp
import com.pnuema.android.codingchallenge.persistance.FavoritesDatabase
import com.pnuema.android.codingchallenge.persistance.daos.Favorite
import com.pnuema.android.codingchallenge.details.ui.DetailsActivity
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import kotlinx.android.synthetic.main.location_result_item.view.*
import java.util.concurrent.Executors

/**
 * View holder for display of the location information on the main screen of the app in a list.
 * Allows favoriting, and navigation to the detail screen
 */
class LocationResultViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_result_item, parent, false)) {
    fun bind(locationResult: LocationResult) {
        //name and category display
        itemView.locationName.text = locationResult.locationName
        itemView.locationCategory.text = locationResult.locationCategory

        //distance display
        locationResult.locationDistance?.let {
            itemView.locationDistance.text = itemView.context.resources.getQuantityString(R.plurals.meters, it.toInt(), it.toInt())
        }

        //load the image async with Glide so that the UI doesnt have to wait around on images to load (GlideConfig.kt)
        GlideApp.with(itemView.context).load(locationResult.locationIcon).into(itemView.locationImage)

        //set the initial state of the favorites icon by checking if its a favorite in the database
        locationResult.id?.let {
            Executors.newSingleThreadExecutor().submit {
                itemView.locationFavorite.isChecked = locationResult.id == FavoritesDatabase.database(itemView.context).favoritesDao().getFavoriteById(it).id
            }
        }

        //handle the status changes for favorites when the user clicks the star
        itemView.locationFavorite.setOnCheckedChangeListener { buttonView, isChecked ->
            locationResult.id?.let {
                if (isChecked) {
                    //add to database if not already in there
                    Executors.newSingleThreadExecutor().submit {
                        FavoritesDatabase.database(itemView.context).favoritesDao().addFavorite(favorite = Favorite(locationResult.id))
                    }
                } else {
                    //remove from database
                    Executors.newSingleThreadExecutor().submit {
                        FavoritesDatabase.database(itemView.context).favoritesDao().removeFavoriteById(locationResult.id)
                    }
                }

                return@setOnCheckedChangeListener
            }

            //cant set this favorite or unset it due to bad location id
            buttonView.isChecked = !isChecked //reset displayed state so to keep the data in sync
            Toast.makeText(itemView.context, R.string.error_favoriting, Toast.LENGTH_LONG).show()
        }

        //launch the details screen on click
        itemView.setOnClickListener {
            locationResult.id?.let {id ->
                DetailsActivity.launch(itemView.context, id)
            }
        }
    }
}