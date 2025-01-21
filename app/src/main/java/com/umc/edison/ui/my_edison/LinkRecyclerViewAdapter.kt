package com.umc.edison.ui.my_edison

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.databinding.TitleItemBinding
import com.umc.edison.domain.model.Bubble
import com.umc.edison.local.model.BubbleLocal

class LinkRecyclerViewAdapter (
    private val items: MutableList<Bubble>,
    private val onItemClick: (Bubble) -> Unit
) : RecyclerView.Adapter<LinkRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: TitleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTextView: TextView = binding.titleTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TitleItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = "[["+item.title+"]]"
        holder.titleTextView.setOnLongClickListener {
            onItemClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size


    fun updateItems(newItem: Bubble) {

        items.add(newItem)
        notifyItemInserted(items.size - 1)
    }



}