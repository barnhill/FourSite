package com.pnuema.android.codingchallenge.helpers

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Utilities to help with displaying and manipulating Google Maps objects
 */
class MapUtils {
    companion object {
        /**
         * Add a marker in Seattle and move the camera to the location
         */
        fun setPivotMarker(googleMap: GoogleMap) {
            val seattle = pivotLatLong()
            googleMap.addMarker(
                MarkerOptions()
                    .position(seattle)
                    .title("Marker in Seattle")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(seattle))
        }

        /**
         * Add marker to location
         */
        fun setMarker(googleMap: GoogleMap, lat: Double, lng: Double, title: String?): Marker {
            val loc = LatLng(lat, lng)
            return googleMap.addMarker(MarkerOptions()
                .position(loc)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
        }

        /**
         * Get the pivot location latitude and longitude
         */
        fun pivotLatLong(): LatLng {
            return LatLng(47.6062, -122.3321)
        }
    }
}