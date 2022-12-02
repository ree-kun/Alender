package com.example.customalarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.dialog.list.RepeatUnit.*
import com.example.customalarm.receiver.AlarmReceiver
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

object Util {

    fun scheduleAlarm(context: Context, entity: AlarmSettingEntity) {
        val time = calcAlarmDateTime(entity) ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.data = Uri.parse(entity.id.toString())

        var pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) pendingFlags =
            pendingFlags or PendingIntent.FLAG_IMMUTABLE
        val alarmIntent = PendingIntent.getBroadcast(context, entity.id.toInt(), intent, pendingFlags)

        val timeInMillis = time.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()

        // TODO 電源OFF時などを考慮して、電力消費が少ない方法に変更する。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(timeInMillis, null), alarmIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,timeInMillis, alarmIntent)
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, timeInMillis] = alarmIntent
        }
    }

    // TODO 同日中に複数回繰り返す場合を考慮する
    private fun calcAlarmDateTime(entity: AlarmSettingEntity): LocalDateTime? {
        val firstTargetTime = entity.startDate.atTime(entity.time)
        val identifier = entity.generator.generate(firstTargetTime)

        val todayTargetTime = LocalDate.now().atTime(entity.time)

        return if (firstTargetTime.isBefore(todayTargetTime)) {
            if (entity.generator.repeatUnit() == NO_REPEAT) return null

            var targetDateTime = todayTargetTime
            do {
                targetDateTime = targetDateTime.plusDays(1)
            } while (!identifier.isTarget(targetDateTime.toLocalDate()))
            targetDateTime
        } else {
            firstTargetTime
        }
    }
}