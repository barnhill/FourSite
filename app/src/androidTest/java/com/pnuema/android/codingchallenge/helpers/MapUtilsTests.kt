package com.pnuema.android.codingchallenge.helpers

import com.pnuema.android.codingchallenge.helpers.MapUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MapUtils
 */
class MapUtilsTests {
    @Before
    fun setupTests() {
    }

    @Test
    fun testLocationMarkerForSeattle_latitude() {
        assertEquals(MapUtils.pivotLatLong().latitude, 47.6062, 0.001)
    }

    @Test
    fun testLocationMarkerForSeattle_longitude() {
        assertEquals(MapUtils.pivotLatLong().longitude, -122.3321, 0.001)
    }
}
