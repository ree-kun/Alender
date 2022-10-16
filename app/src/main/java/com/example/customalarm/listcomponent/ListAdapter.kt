package com.example.customalarm.listcomponent

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.InputActivity
import com.example.customalarm.R
import com.example.customalarm.common.EditMode
import com.example.customalarm.entity.AlarmSetting

class ListAdapter(private val data: ArrayList<AlarmSetting>) :
    RecyclerView.Adapter<ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_setting, parent, false)
        val holder = ListViewHolder(v)
        holder.itemView.setOnClickListener {
            // TODO 編集処理への遷移
            val context = parent.context
            val position: Int = holder.adapterPosition
            val i = Intent(context, InputActivity::class.java)
            i.putExtra("editMode", EditMode.EDIT_MODE)
            i.putExtra("alarmId", data[position].getId())
            (context as Activity).startActivityForResult(i, EditMode.EDIT_MODE)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        Log.d("debug", data[position].getTitle())
        holder.title.text = data[position].getTitle()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
