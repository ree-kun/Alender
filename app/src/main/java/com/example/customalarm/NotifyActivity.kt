package com.example.customalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotifyActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var alarmSettingDao: AlarmSettingDao
    private lateinit var tts: TextToSpeech

    private lateinit var speechText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notify)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()
        tts = TextToSpeech(this, this)

        val alarmId = intent.getIntExtra("alarmId", -1)

//        val ttsButton = findViewById<Button>(R.id.stopBtn)
//        ttsButton.setOnClickListener(this)

        scope.launch {
            val alarmSettingEntity = alarmSettingDao.selectById(alarmId)
            speechText = alarmSettingEntity.title
        }
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    override fun onInit(status: Int) {
        if (TextToSpeech.SUCCESS == status) {
            Log.d("debug", "initialized")
            tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "notifyMessage")
            finish()
        } else {
            Log.d("debug", "failed to initialized")
        }
    }

}