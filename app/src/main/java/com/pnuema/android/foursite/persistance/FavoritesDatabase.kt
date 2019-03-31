package com.pnuema.android.foursite.persistance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pnuema.android.foursite.persistance.daos.Favorite
import com.pnuema.android.foursite.persistance.daos.FavoriteDAO

@Database(entities = [Favorite::class], version = 1, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoriteDAO

    companion object {
        private var INSTANCE: FavoritesDatabase? = null

        fun database(context: Context): FavoritesDatabase {
            if (INSTANCE == null) {
                synchronized(FavoritesDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, FavoritesDatabase::class.java, "favsdb").build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}