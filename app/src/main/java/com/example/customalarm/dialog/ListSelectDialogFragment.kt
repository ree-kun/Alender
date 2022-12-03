package com.example.customalarm.dialog

import android.app.Dialog
import android.os.Bundle
import com.example.customalarm.dialog.list.ListOption

// TODO コンストラクタで値を注入するのを辞める
//  https://zenn.dev/m_coder/articles/article-zenn-custom-dialog-by-dialogfragment
class ListSelectDialogFragment<T : ListOption>(
    title: String,
    private val listOptions: Array<T>
) : AbstractDialogFragment<T>(title) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return builder()
            .setItems(listOptions.map { it.text }.toTypedArray()) { dialog, i ->
                val item = listOptions.filter { it.text == listOptions[i].text }[0]
                println("dialog:$dialog which:$id $item")
                lister.onDialogSelect(item)
            }
            .create()
    }

}
