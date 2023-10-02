package com.example.android_take_home_exercise_shanglin_yang

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var items: MutableList<Item> = mutableListOf()

    fun setItems(newItems: MutableList<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 在这里绑定视图元素和数据
        fun bind(item: Item) {
            val listIdTextView: TextView = itemView.findViewById(R.id.listIdTextView)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

            listIdTextView.text = item.listId.toString()
            nameTextView.text = item.name
        }
    }
}
