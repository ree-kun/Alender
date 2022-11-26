package com.example.customalarm.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.customalarm.R
import com.example.customalarm.calendar.logic.dto.NthDay
import com.example.customalarm.dialog.list.Day

class MonthlyWeekRepeatDialogFragment(
    title: String
) : AbstractDialogFragment<Pair<List<NthDay>, List<Day>>>(title) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val monthlyWeekRepeatLayout = LayoutInflater.from(activity).inflate(R.layout.dialog_repeat_monthly_week, null)
        val weekSelect = monthlyWeekRepeatLayout.findViewById<LinearLayout>(R.id.weekSelect)
        val daySelect = monthlyWeekRepeatLayout.findViewById<LinearLayout>(R.id.daySelect)

        val weekOption = ((1..5) + (-1..-1)).map { NthDay(it) }
        weekOption.forEach {
            val checkBox = CheckBox(activity)
            checkBox.text = it.text
            checkBox.setPadding(10, 0, 0, 0)
            // TODO 初期値を登録済みの値に変更する（未登録の場合は全てfalseとする）
            checkBox.isChecked = false
            weekSelect.addView(checkBox)
        }

        Day.values().forEach { day ->
            val checkBox = CheckBox(activity)
            checkBox.text = day.text
            checkBox.setPadding(10, 0, 0, 0)
            // TODO 初期値を登録済みの値に変更する（未登録の場合は全てfalseとする）
            checkBox.isChecked = false
            daySelect.addView(checkBox)
        }

        return builder()
            .setView(monthlyWeekRepeatLayout)
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(
                    Pair(
                        weekOption.filterIndexed { i, _ ->
                            (weekSelect.getChildAt(i) as CheckBox).isChecked
                        },
                        Day.values().filterIndexed { i, _ ->
                            (daySelect.getChildAt(i) as CheckBox).isChecked
                        }
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()
    }

}
