package com.umc.edison.ui.my_edison

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.local.model.BubbleLocal

class TitleRecyclerViewAdapter(
    private val list: ArrayList<BubbleLocal>,
    private val onItemClick: (BubbleLocal) -> Unit
) : RecyclerView.Adapter<TitleRecyclerViewAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.title_item, parent, false) // 아이템 레이아웃 설정
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = list[position]
        holder.titleTextView.text = "[[ " + item.title + " ]]"
        holder.itemView.setOnClickListener { onItemClick(item) }// BubbleLocal의 title만 표시
    }

    override fun getItemCount(): Int = list.size
}