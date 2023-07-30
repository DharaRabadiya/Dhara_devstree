package com.dhara.devstree.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhara.devstree.R
import com.dhara.devstree.datamodel.Item
import com.dhara.devstree.db.AppDatabase
import com.dhara.devstree.repo.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemAdapter(var items: List<Item>, private val listener:
OnItemClickListener,var context:Context) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int,item: Item,update:String,delete:String)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val btnUpdate: TextView = itemView.findViewById(R.id.btnUpdate)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
        val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    listener.onItemClick(position,item,"","")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]
        holder.nameTextView.text = currentItem.place_description
        holder.distanceTextView.text = currentItem.distance.toString() + " Km"
        holder.btnUpdate.setOnClickListener(View.OnClickListener {
            val item = items[position]
            listener.onItemClick(position,item,"update","")
        })
        holder.btnDelete.setOnClickListener(View.OnClickListener {
                val item = items[position]
                listener.onItemClick(position,item,"","delete")
        })
        //holder.descriptionTextView.text = currentItem.description
    }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}