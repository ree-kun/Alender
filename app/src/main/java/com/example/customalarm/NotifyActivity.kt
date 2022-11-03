package com.example.customalarm

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotifyActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener, TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var alarmSettingDao: AlarmSettingDao
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var tts: TextToSpeech

    private lateinit var stopBtn: Button
    private lateinit var speechText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO 適切な設定を確認する。
        window.addFlags(
            (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        )

        setContentView(R.layout.activity_notify)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()
        tts = TextToSpeech(this, this)

        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
        mediaPlayer!!.setOnCompletionListener(this)

        val alarmId = intent.getIntExtra("alarmId", -1)
        scope.launch {
            val alarmSettingEntity = alarmSettingDao.selectById(alarmId)
            speechText = alarmSettingEntity.title
        }

        play()

        stopBtn = findViewById(R.id.stopBtn)
        stopBtn.setOnClickListener {
            tts.stop()
            tts.shutdown()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private fun play() {
        mediaPlayer!!.start()
    }

    private fun stop() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        stop()
        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "notifyMessage")
    }

    override fun onInit(status: Int) {
        if (TextToSpeech.SUCCESS == status) {
            println("debug: initialized")
            setTtsListener()
        } else {
            println("debug: failed to initialized")
        }
    }

    private fun setTtsListener() {
        val listenerResult = tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                Log.d("debug", "progress on Done $utteranceId")
                this@NotifyActivity.finish()
            }

            override fun onError(utteranceId: String) {
                Log.d("debug", "progress on Error $utteranceId")
            }

            override fun onStart(utteranceId: String) {
                Log.d("debug", "progress on Start $utteranceId")
            }
        })

        if (listenerResult != TextToSpeech.SUCCESS) {
            Log.e("debug", "faild to add utterance progress listener")
        }
    }

}