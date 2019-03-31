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
        fun setPivotMarker(label: String, googleMap: GoogleMap, lat: Double, lng: Double) {
            setMarker(googleMap, lat, lng, label, BitmapDescriptorFactory.HUE_AZURE)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
        }

        /**
         * Add standard marker to location
         */
        fun setMarker(googleMap: GoogleMap, lat: Double, lng: Double, title: String?): Marker {
            return setMarker(googleMap, lat, lng, title, BitmapDescriptorFactory.HUE_ORANGE)
        }

        /**
         * Add marker to location
         */
        fun setMarker(googleMap: GoogleMap, lat: Double, lng: Double, title: String?, color: Float): Marker {
            val loc = LatLng(lat, lng)
            return googleMap.addMarker(MarkerOptions()
                .position(loc)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
        }
    }
}