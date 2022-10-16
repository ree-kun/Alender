package com.example.customalarm.listcomponent

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.R

class ListViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var title: TextView

    init {
        title = itemView.findViewById(R.id.title)
    }
}