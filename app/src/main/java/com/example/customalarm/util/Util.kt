package com.example.customalarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
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
        val alarmIntent = createIntent(context, entity.id.toInt())

        val timeInMillis = time.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("debug", "setExactAndAllowWhileIdle")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent)
        } else {
            Log.d("debug", "setExact")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = createIntent(context, alarmId.toInt())

        alarmManager.cancel(alarmIntent)
    }

    private fun createIntent(context: Context, alarmId: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.data = Uri.parse(alarmId.toString())

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

        return PendingIntent.getBroadcast(context, alarmId, intent, pendingFlags)
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