package com.example.customalarm.dialog

import android.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

abstract class AbstractDialogFragment<T>(
    private val title: String
) : DialogFragment() {

    protected lateinit var lister: InputDialogLister<T>

    fun builder(): AlertDialog.Builder {
        val builder = AlertDialog.Builder(activity)
        return builder.setTitle(title)
    }

    fun execute(fragmentManager: FragmentManager) {
        this.show(fragmentManager, title)
    }

    fun onSubmit(lister: InputDialogLister<T>): AbstractDialogFragment<T> {
        this.lister = lister
        return this
    }

}
