package com.pnuema.android.codingchallenge.mainscreen.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pnuema.android.codingchallenge.mainscreen.ui.models.LocationResult
import com.pnuema.android.codingchallenge.mainscreen.ui.viewholders.LocationResultViewHolder

/**
 * Adapter for creating and displaying view holders on the main page for search results
 */
class SearchResultsAdapter: RecyclerView.Adapter<LocationResultViewHolder>() {
    private var locationDataItems: List<LocationResult> = ArrayList()

    fun setLocationResults(locations: List<LocationResult>) {
        val oldItems = ArrayList(locationDataItems)
        locationDataItems = locations

        val callback = object : DiffCallback<LocationResult>(oldItems, locationDataItems) {}

        DiffUtil.calculateDiff(callback, true).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationResultViewHolder {
        return LocationResultViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return locationDataItems.size
    }

    override fun onBindViewHolder(holder: LocationResultViewHolder, position: Int) {
        holder.bind(locationDataItems[position])
    }
}