package com.umc.edison.ui.my_edison

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.databinding.LabelItemBinding
import com.umc.edison.databinding.TitleItemBinding
import com.umc.edison.domain.model.Bubble
import com.umc.edison.local.model.BubbleLocal

class TitleRecyclerViewAdapter(
    private val list: List<Bubble>,
    private val onItemClick: (Bubble) -> Unit
) : RecyclerView.Adapter<TitleRecyclerViewAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(private val binding:TitleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTextView: TextView = binding.titleTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = TitleItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = list[position]
        holder.titleTextView.text =  "[["+item.title+"]]"
        holder.titleTextView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}