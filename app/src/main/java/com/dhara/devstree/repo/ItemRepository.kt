package com.dhara.devstree.repo

import androidx.lifecycle.LiveData
import com.dhara.devstree.dao.ItemDao
import com.dhara.devstree.datamodel.Item

class ItemRepository(private val itemDao: ItemDao) {

    val allItems: LiveData<List<Item>> = itemDao.getAllItems()

    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }

    suspend fun update(item: Item) {
        itemDao.update(item)
    }

    suspend fun delete(item: Item) {
        itemDao.delete(item)
    }
}