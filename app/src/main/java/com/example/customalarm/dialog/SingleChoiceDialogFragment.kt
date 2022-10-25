package com.example.customalarm.dialog

import android.app.Dialog
import android.os.Bundle
import com.example.customalarm.dialog.list.ListOption

class SingleChoiceDialogFragment<T : ListOption>(
    title: String,
    private val listOptions: Array<T>
) : AbstractDialogFragment<T>(title) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // TODO 初期値を登録済みの値に変更する（未登録の場合は全てfalseでOK）
        var checkedItem = 0

        return builder()
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
            .create()
    }

}
