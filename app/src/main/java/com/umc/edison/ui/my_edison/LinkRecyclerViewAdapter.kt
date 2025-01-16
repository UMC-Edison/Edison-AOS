package com.umc.edison.ui.my_edison

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.local.model.BubbleLocal

class LinkRecyclerViewAdapter (
    private val items: MutableList<BubbleLocal>
) : RecyclerView.Adapter<LinkRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.title_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
    }

    override fun getItemCount(): Int = items.size

    // 데이터 업데이트 메서드
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<BubbleLocal>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}