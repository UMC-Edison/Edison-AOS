package com.umc.edison.ui.my_edison

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.databinding.LabelItemBinding
import com.umc.edison.domain.model.Label
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.LabelLocal

class LabelRecyclerViewAdapter(
    private val list: List<Label>,
    private val selectedLabel: MutableList<Label>,
    private val onItemClick: (Label) -> Unit,
) : RecyclerView.Adapter<LabelRecyclerViewAdapter.CustomViewHolder>() {

    class CustomViewHolder(private val binding: LabelItemBinding) : RecyclerView.ViewHolder(binding.root){

        val labelView : Button = binding.labelBt

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabelRecyclerViewAdapter.CustomViewHolder {
        val view = LabelItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CustomViewHolder,
        position: Int
    ) {
        val item = list[position]
        holder.labelView.text = item.name
        val baseColor = Color.parseColor(item.color)

        val backgroundDrawable = holder.labelView.background
        if (backgroundDrawable is GradientDrawable) {
            backgroundDrawable.setColor(Color.parseColor(item.color))

            // 선택 상태에 따라 테두리 추가
            if (selectedLabel.contains(item)) {

                backgroundDrawable.setColor(darkenColor(baseColor, 0.7f))
                //backgroundDrawable.setStroke(4, Color.parseColor("#E8E8E8")) // 테두리 추가
            } else {

                backgroundDrawable.setColor(baseColor)
                backgroundDrawable.setStroke(0, Color.TRANSPARENT) // 테두리 제거
            }
        }


        holder.labelView.setOnClickListener {
            onItemClick(item)

        }
    }

    override fun getItemCount(): Int = list.size

    }

fun darkenColor(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).toInt()
    val g = (Color.green(color) * factor).toInt()
    val b = (Color.blue(color) * factor).toInt()
    return Color.argb(a, r, g, b)
}