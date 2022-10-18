package com.example.customalarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.receiver.AlarmReceiver
import java.util.*

object Util {

    fun scheduleAlarm(context: Context, item: AlarmSettingEntity) {
        var time = Calendar.getInstance()
        time.set(Calendar.HOUR_OF_DAY, item.time.split(":")[0].toInt())
        time.set(Calendar.MINUTE, item.time.split(":")[1].toInt())
        time.set(Calendar.SECOND, 0)

        val now = Calendar.getInstance()
        // 当日の設定時刻を過ぎている場合は次の設定時刻へ変更 TODO 同日中に複数回繰り返す場合の考慮漏れ
        if (time.before(now)) time = calcNextAlarmTime(time)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.data = Uri.parse(item.toString())

        var pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) pendingFlags =
            pendingFlags or PendingIntent.FLAG_IMMUTABLE
        val alarmIntent = PendingIntent.getBroadcast(context, item.id, intent, pendingFlags)

        // TODO 電源OFF時などを考慮して、電力消費が少ない方法に変更する。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, alarmIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(time.timeInMillis, null), alarmIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.timeInMillis, alarmIntent)
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, time.timeInMillis] = alarmIntent
        }
    }

    private fun calcNextAlarmTime(prev: Calendar): Calendar {
        prev.set(Calendar.DATE, prev.get(Calendar.DATE) +  1)
        return prev
    }
}