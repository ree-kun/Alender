package com.example.customalarm

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.util.DatabaseHelper
import com.example.customalarm.util.DatabaseHelper.Companion.TABLE_NAME

class InputActivity : AppCompatActivity() {

    private lateinit var helper: DatabaseHelper

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        helper = DatabaseHelper.getInstance(this)

        settingTimeDrum()
        settingOperationButton()
    }

    private fun settingTimeDrum() {
        // NumberPickerを取得
        hourPicker = findViewById(R.id.hourPicker)
        minutePicker = findViewById(R.id.minutePicker)

        // 配列のインデックス最小、最大を指定
        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        // ドラムロール表示用の配列作成
        val minutes = arrayOf("0", "15", "30", "45")
        // 配列のインデックス最小、最大を指定
        minutePicker.minValue = 0
        minutePicker.maxValue = minutes.size - 1
        // NumberPickerに配列をセットする
        minutePicker.displayedValues = minutes
    }

    private fun settingOperationButton() {
        cancelButton = findViewById(R.id.cancelButton)
        addButton = findViewById(R.id.addButton)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        addButton.setOnClickListener {
            val hour = hourPicker.value
            val minute = minutePicker.value
            val editAlarmTitle = findViewById<EditText>(R.id.editAlarmTitle).text.toString()

            Log.d("debug", editAlarmTitle)
            // データ登録
            try {
                helper.writableDatabase.use { db ->
                    val cv = ContentValues()
                    cv.put("title", editAlarmTitle)
                    db.insert(TABLE_NAME, null, cv).toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            setResult(RESULT_OK, Intent())
            finish()
        }
    }
}
