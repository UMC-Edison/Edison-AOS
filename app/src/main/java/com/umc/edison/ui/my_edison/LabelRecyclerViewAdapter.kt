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
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.LabelLocal

class LabelRecyclerViewAdapter(
    private val list: ArrayList<LabelLocal>,
    private val selectedLabel: MutableList<LabelLocal>,
   private val onItemClick: (LabelLocal) -> Unit,
) : RecyclerView.Adapter<LabelRecyclerViewAdapter.CustomViewHolder>() {

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val labelView : Button = itemView.findViewById(R.id.labelBt)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabelRecyclerViewAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.label_item, parent, false)

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

                backgroundDrawable.setColor(darkenColor(baseColor, 0.8f))
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