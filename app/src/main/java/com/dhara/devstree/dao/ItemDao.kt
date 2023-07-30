package com.dhara.devstree.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.dhara.devstree.datamodel.Item

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getAllItems(): LiveData<List<Item>>
}