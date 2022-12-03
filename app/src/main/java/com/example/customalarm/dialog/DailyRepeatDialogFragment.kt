package com.example.customalarm.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import com.example.customalarm.calendar.logic.dto.DailyPitch

class DailyRepeatDialogFragment(
    title: String
) : AbstractDialogFragment<DailyPitch>(title) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = NumberPicker(activity)
        val pitches = (1..100).map { DailyPitch(it) }
        picker.value = 0
        picker.minValue = 0
        picker.maxValue = pitches.size - 1
        picker.displayedValues = pitches.map { it.text }.toTypedArray()
        picker.wrapSelectorWheel = false

        return builder()
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(pitches[picker.value])
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()
    }

}
