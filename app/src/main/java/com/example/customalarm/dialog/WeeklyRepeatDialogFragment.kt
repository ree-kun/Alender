package com.example.customalarm.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.NumberPicker
import com.example.customalarm.R
import com.example.customalarm.dialog.list.Day

class WeeklyRepeatDialogFragment(
    private val title: String
) : AbstractDialogFragment() {

    private lateinit var lister: InputDialogLister<List<Day>>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val weekRepeatLayout = LayoutInflater.from(activity).inflate(R.layout.dialog_repeat_week, null)
        val daySelect = weekRepeatLayout.findViewById<LinearLayout>(R.id.daySelect)

        val weeklyPitch = weekRepeatLayout.findViewById<NumberPicker>(R.id.weeklyPitch)
        val repeatTypes = Array(100) {
            when (it) {
                0 -> "毎週"
                1 -> "隔週"
                else -> "${it + 1}週間ごと"
            }
        }
        weeklyPitch.minValue = 0
        weeklyPitch.maxValue = repeatTypes.size - 1
        weeklyPitch.displayedValues = repeatTypes
        weeklyPitch.wrapSelectorWheel = false

        Day.values().forEach { day ->
            val checkBox = CheckBox(activity)
            checkBox.text = day.text
            checkBox.setPadding(10, 0, 0, 0)
            // TODO 初期値を登録済みの値に変更する（未登録の場合は全てfalseとする）
            checkBox.isChecked = false
            daySelect.addView(checkBox)
        }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
            .setView(weekRepeatLayout)
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(
                    Day.values()
                        .filterIndexed { i, _ ->
                            (daySelect.getChildAt(i) as CheckBox).isChecked
                        }
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
        return builder.create()
    }

    fun onSubmit(lister: InputDialogLister<List<Day>>): WeeklyRepeatDialogFragment {
        this.lister = lister
        return this
    }

}
