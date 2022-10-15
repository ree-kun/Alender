package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // フローティングアクションボタンの設定
        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val i = Intent(this, InputActivity::class.java)
            startActivity(i)
        }
    }
}
