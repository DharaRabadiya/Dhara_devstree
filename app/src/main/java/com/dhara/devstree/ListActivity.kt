package com.dhara.devstree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhara.devstree.R
import com.dhara.devstree.adapter.ItemAdapter
import com.dhara.devstree.datamodel.Item
import com.dhara.devstree.db.AppDatabase
import com.dhara.devstree.repo.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() , ItemAdapter.OnItemClickListener{
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemRepository: ItemRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdd: Button
    private lateinit var sortA: Button
    private lateinit var sortD: Button
    private lateinit var showPath: Button
    val myCoroutineScope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        findById()
        setData()
        setListener()
    }

    override fun onItemClick(position:Int,item: Item,update:String,delete:String) {
        if(update.equals("update")){
            val intent = Intent(this, MainActivity::class.java)

            if(position > 0){
                intent.putExtra("first_lat",itemAdapter.items.get(0).place_lat)
                intent.putExtra("first_lng",itemAdapter.items.get(0).place_lng)
            }

            intent.putExtra("lat",item.place_lat)
            intent.putExtra("lng",item.place_lng)
            intent.putExtra("marker_title",item.place_description)
            intent.putExtra("place_id",item.place_id)
            intent.putExtra("id",item.id)
            startActivity(intent)
        }
        else if(delete.equals("delete")){
            myCoroutineScope.launch {
                itemRepository.delete(item)
                itemAdapter.notifyDataSetChanged()
            }
        }
        else{
            val intent = Intent(this, MainActivity::class.java)

            if(position > 0){
                intent.putExtra("first_lat",itemAdapter.items.get(0).place_lat)
                intent.putExtra("first_lng",itemAdapter.items.get(0).place_lng)
            }

            intent.putExtra("lat",item.place_lat)
            intent.putExtra("lng",item.place_lng)
            intent.putExtra("marker_title",item.place_description)
            intent.putExtra("place_id",item.place_id)
            intent.putExtra("id",item.id)
            startActivity(intent)
        }
    }

    fun setData(){
        val itemDao = AppDatabase.getDatabase(applicationContext).itemDao()
        itemRepository = ItemRepository(itemDao)

        itemAdapter = ItemAdapter(emptyList(), this,applicationContext)
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        itemRepository.allItems.observe(this, { items ->
            itemAdapter.items = items
            itemAdapter.notifyDataSetChanged()
        })
    }

    fun findById(){
        recyclerView = findViewById(R.id.recyclerView)
        btnAdd = findViewById(R.id.btnAdd)
        showPath = findViewById(R.id.showPath)
        sortA = findViewById(R.id.sortA)
        sortD = findViewById(R.id.sortD)
    }
    fun setListener(){

        sortA.setOnClickListener(View.OnClickListener {
            val sortedListAscending = itemAdapter.items.sortedBy { it.distance }
            itemAdapter.updateList(sortedListAscending)
        })


        sortD.setOnClickListener(View.OnClickListener {
            val sortedListDescending  = itemAdapter.items.sortedByDescending { it.distance }
            itemAdapter.updateList(sortedListDescending )
        })

        btnAdd.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            if(itemAdapter.items.size > 0){
                intent.putExtra("first_lat",itemAdapter.items.get(0).place_lat)
                intent.putExtra("first_lng",itemAdapter.items.get(0).place_lng)
            }
            startActivity(intent)
        })

        showPath.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("show_path","true")
            startActivity(intent)
        })
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}