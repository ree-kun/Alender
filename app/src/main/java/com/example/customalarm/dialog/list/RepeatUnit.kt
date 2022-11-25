package com.example.customalarm.dialog.list

enum class RepeatUnit(
    override val text: String
) : ListOption {

    NO_REPEAT("繰り返さない"),
    DAILY("毎日/◯日ごと"),
    WEEKLY("毎週/◯週ごと ◯曜日"),
    MONTHLY_DAY("毎月◯日"),
    MONTHLY_NTH_DAY("毎月第◯ ◯曜日"),
//    YEARLY("年ごとに設定"),
    ;

    companion object {
        fun of(name: String): RepeatUnit? {
            return values().find { it.name == name }
        }
    }

}
