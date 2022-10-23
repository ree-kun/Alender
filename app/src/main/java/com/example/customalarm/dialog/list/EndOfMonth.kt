package com.example.customalarm.dialog.list

enum class EndOfMonth(
    override val text: String
) : ListOption {

    TO_LAST_DAY("その月の末日に設定する"),
    TO_NEXT_FIRST_DAY("翌月の1日に設定する"),
    NO_SET("設定しない"),

}
