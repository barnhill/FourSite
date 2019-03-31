package com.pnuema.android.foursite.persistance

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pnuema.android.foursite.persistance.daos.Favorite
import com.pnuema.android.foursite.persistance.daos.FavoriteDAO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Tests for the Room database
 */
class DatabaseTests {
    private lateinit var favoriteDAO: FavoriteDAO
    private lateinit var db: FavoritesDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, FavoritesDatabase::class.java).allowMainThreadQueries().build()
        favoriteDAO = db.favoritesDao()
    }

    @Test
    @Throws(Exception::class)
    fun writeFavoriteAndRead() {
        val favorite = Favorite("1")
        favoriteDAO.addFavorite(favorite)
        val retrievedFav = favoriteDAO.getFavoriteById("1")
        assertEquals(retrievedFav?.id, favorite.id)
    }

    @Test
    @Throws(Exception::class)
    fun readNonFavorite() {
        val retrievedFav = favoriteDAO.getFavoriteById("345")
        assertNull(retrievedFav)
    }

    @Test
    @Throws(Exception::class)
    fun writeFavoriteAndRemove() {
        val favorite = Favorite("1")
        favoriteDAO.addFavorite(favorite)
        favoriteDAO.removeFavoriteById("1")
        assertNull(favoriteDAO.getFavoriteById("1"))
    }
}
