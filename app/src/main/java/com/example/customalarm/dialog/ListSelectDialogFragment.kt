package com.example.customalarm.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.example.customalarm.dialog.list.ListOption

// TODO コンストラクタで値を注入するのを辞める
//  https://zenn.dev/m_coder/articles/article-zenn-custom-dialog-by-dialogfragment
class ListSelectDialogFragment<T : ListOption>(
    private val title: String,
    private val listOptions: Array<T>
) : AbstractDialogFragment() {

    private lateinit var lister: InputDialogLister<T>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
            .setItems(listOptions.map { it.text }.toTypedArray()) { dialog, i ->
                val item = listOptions.filter { it.text == listOptions[i].text }[0]
                println("dialog:$dialog which:$id $item")
                lister.onDialogSelect(item)
            }
        return builder.create()
    }

    fun onSelected(lister: InputDialogLister<T>): ListSelectDialogFragment<T> {
        this.lister = lister
        return this
    }

}
