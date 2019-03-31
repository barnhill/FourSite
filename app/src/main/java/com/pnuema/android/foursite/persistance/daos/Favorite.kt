package com.pnuema.android.foursite.persistance.daos

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index

@Entity(
    tableName = "favorites",
    primaryKeys = ["id"],
    indices = [Index(value = ["id"])]
)
class Favorite() {
    @Ignore
    constructor(id: String) : this() {
        this.id = id
    }
    @NonNull
    @ColumnInfo(name = "id")
    var id: String? = null
}