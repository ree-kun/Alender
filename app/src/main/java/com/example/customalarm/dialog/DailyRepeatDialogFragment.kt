package com.example.customalarm.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker

class DailyRepeatDialogFragment(
    title: String
) : AbstractDialogFragment<Int>(title) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = NumberPicker(activity)
        val repeatTypes = Array(100) {
            when (it) {
                0 -> "毎日"
                else -> "${it + 1}日ごと"
            }
        }
        picker.value = 0
        picker.minValue = 0
        picker.maxValue = repeatTypes.size - 1
        picker.displayedValues = repeatTypes
        picker.wrapSelectorWheel = false

        return builder()
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(picker.value)
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()
    }

}
