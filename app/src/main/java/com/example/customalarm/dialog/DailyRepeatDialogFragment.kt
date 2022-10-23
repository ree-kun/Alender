package com.example.customalarm.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker

class DailyRepeatDialogFragment(
    private val title: String
) : AbstractDialogFragment() {

    private lateinit var lister: InputDialogLister<Int>

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

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(picker.value)
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
        return builder.create()
    }

    fun onSubmit(lister: InputDialogLister<Int>): DailyRepeatDialogFragment {
        this.lister = lister
        return this
    }

}
