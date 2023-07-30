package com.dhara.devstree.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.dhara.devstree.PlaceItem

class SearchAutoCompleteAdapter(
    context: Context,
    resource: Int,
    private val resultList: List<PlaceItem> = emptyList()
) : ArrayAdapter<PlaceItem>(context, resource, resultList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)

        val placeItem = getItem(position)
        placeItem?.let {
            view.findViewById<TextView>(android.R.id.text1)?.text = it.primaryText
        }

        return view
    }
}
