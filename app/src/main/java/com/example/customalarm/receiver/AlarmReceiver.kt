package com.example.customalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.customalarm.NotifyActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("debug", intent.data.toString())
        val alarmId = intent.data.toString().toLong()
        Log.d("debug", alarmId.toString())

        val i = Intent(context, NotifyActivity::class.java)
        i.putExtra("alarmId", alarmId)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(i)
    }
}
