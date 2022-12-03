package com.example.customalarm.dialog

import android.app.Dialog
import android.os.Bundle
import com.example.customalarm.dialog.list.ListOption

class MultiChoiceDialogFragment<T : ListOption>(
    title: String,
    private val listOptions: Array<T>
) : AbstractDialogFragment<List<T>>(title) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // TODO 初期値を登録済みの値に変更する（未登録の場合は全てfalseでOK）
        val checkedItems = listOptions.map { false }.toBooleanArray()
        val selectedItems: MutableList<Int> = mutableListOf()

        return builder()
            .setMultiChoiceItems(
                listOptions.map { it.text }.toTypedArray(), checkedItems
            ) { _, i, isChecked ->
                if (isChecked) {
                    selectedItems.add(i)
                } else {
                    selectedItems.remove(i)
                }
            }
            .setPositiveButton("OK") { _, _ ->
                lister.onDialogSelect(selectedItems.map { listOptions[it] })
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()
    }

}
