package com.example.customalarm.dialog.list

enum class RepeatUnit(
    override val text: String
) : ListOption {

    NO_REPEAT("繰り返さない"),
    DAILY("日ごとに設定"),
    WEEKLY("週,曜日ごとに設定"),
    MONTHLY("月ごとに設定"),
//    YEARLY("年ごとに設定"),

}
