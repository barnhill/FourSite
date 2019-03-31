package com.pnuema.android.foursite.persistance.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDAO {
    @Query("select * from favorites where id = :ID limit 1")
    fun getFavoriteById(ID: String): Favorite?

    @Query("delete from favorites where id = :ID")
    fun removeFavoriteById(ID: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorite(favorite: Favorite)
}