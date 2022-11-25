package com.example.customalarm.dialog.list

enum class Day(
    override val text: String
) : ListOption {

    Sun("日曜日"),
    Mon("月曜日"),
    Tue("火曜日"),
    Wed("水曜日"),
    Thu("木曜日"),
    Fri("金曜日"),
    Sat("土曜日"),
    ;

    companion object {
        fun of(name: String): Day? {
            return values().find { it.name == name }
        }
    }

}
