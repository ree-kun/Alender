package com.example.customalarm.dialog

fun interface InputDialogLister<T> {

    fun onDialogSelect(result: T)

}