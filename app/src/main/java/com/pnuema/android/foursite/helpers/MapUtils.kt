package com.pnuema.android.foursite.helpers

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
         * Add a marker in current location and move the camera to the location
         */
        fun setPivotMarker(label: String, googleMap: GoogleMap, currentLocation: LatLng) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(currentLocation)
                    .title(label)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
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
    }
}