package com.example.customalarm.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.example.customalarm.dialog.list.ListOption

class SingleChoiceDialogFragment<T : ListOption>(
    private val title: String,
    private val listOptions: Array<T>
) : AbstractDialogFragment() {

    private lateinit var lister: InputDialogLister<T>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // TODO 初期値を登録済みの値に変更する（未登録の場合は全てfalseでOK）
        var checkedItem = 0

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
            .setSingleChoiceItems(
                listOptions.map { it.text }.toTypedArray(), checkedItem
            ) { _, i ->
                checkedItem = i
            }
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(listOptions[checkedItem])
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
        return builder.create()
    }

    fun onSubmit(lister: InputDialogLister<T>): SingleChoiceDialogFragment<T> {
        this.lister = lister
        return this
    }

}
