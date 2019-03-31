package com.pnuema.android.foursite.mainscreen.ui.models

import com.pnuema.android.foursite.mainscreen.models.Category
import com.pnuema.android.foursite.mainscreen.models.Icon
import com.pnuema.android.foursite.mainscreen.models.Location
import com.pnuema.android.foursite.mainscreen.models.Venue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Tests for LocationResult
 */
class LocationResultTests {
    lateinit var location: LocationResult
    @Before
    fun setup() {
        val venue = Venue()
        venue.id = "id123"
        venue.name = "venuename"
        venue.location = Location()
        venue.location?.distance = 450
        venue.location?.lat = 45.3333
        venue.location?.lng = -122.433
        venue.categories = ArrayList()
        val category1 = Category()
        category1.id = "id456"
        category1.name = "categoryname1"
        category1.pluralName = "pluralname1"
        category1.icon = Icon()
        category1.icon?.prefix = "prefix"
        category1.icon?.suffix = "suffix"
        (venue.categories as ArrayList).add(category1)
        val category2 = Category()
        category2.id = "id789"
        category2.name = "categoryname2"
        category2.pluralName = "pluralname2"
        category2.icon = Icon()
        category2.icon?.prefix = "prefix"
        category2.icon?.suffix = "suffix"
        (venue.categories as ArrayList).add(category2)
        location = LocationResult(venue)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_id() {
        assertEquals("id123", location.id)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_name() {
        assertEquals("venuename", location.locationName)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_lat() {
        assertEquals(45.3333, location.lat)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_lng() {
        assertEquals(-122.433, location.lng)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_distance() {
        assertEquals(450L, location.locationDistance)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_category() {
        assertEquals("pluralname1", location.locationCategory)
    }

    @Test
    @Throws(Exception::class)
    fun locationResult_iconpath() {
        assertEquals("prefix88suffix", location.locationIcon)
    }
}
