package com.example.customalarm.entity

class AlarmSetting {

    private var id = -1
    private lateinit var title: String

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }
}
