package com.dhara.devstree.datamodel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val place_id: String,
    val place_description: String,
    val place_lat: Double,
    val place_lng: Double,
    var distance: Double,
)