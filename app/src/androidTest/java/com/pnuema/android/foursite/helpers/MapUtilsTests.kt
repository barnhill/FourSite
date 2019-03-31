package com.pnuema.android.foursite.helpers

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
