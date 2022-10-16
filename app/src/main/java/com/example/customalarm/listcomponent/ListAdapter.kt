package com.example.customalarm.listcomponent

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.R
import com.example.customalarm.entity.AlarmSetting

class ListAdapter(private val data: ArrayList<AlarmSetting>) :
    RecyclerView.Adapter<ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.alarm_setting, parent, false)
        val holder = ListViewHolder(v)
        holder.itemView.setOnClickListener {
            // TODO 編集処理への遷移
//            val context = parent.context
//            val position: Int = holder.adapterPosition
//            val i = Intent(context, InputActivity::class.java)
//            i.putExtra(context.getString(R.string.request_code), MainActivity.EDIT_REQ_CODE)
//            i.putExtra(context.getString(R.string.alarm_id), data[position].getAlarmID())
//            (context as Activity).startActivityForResult(i, MainActivity.EDIT_REQ_CODE)
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
