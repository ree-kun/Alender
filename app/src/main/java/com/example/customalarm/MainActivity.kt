package com.example.customalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TTS インスタンス生成
        tts = TextToSpeech(this, this)

        val ttsButton = findViewById<Button>(R.id.button_tts)
        ttsButton.setOnClickListener(this)
    }

    override fun onInit(status: Int) {
        // TTS初期化
        if (TextToSpeech.SUCCESS == status) {
            Log.d("debug", "initialized")
        } else {
            Log.d("debug", "failed to initialized")
        }
    }

    override fun onClick(v: View) {
        speechText()
    }

    private fun shutDown() {
        // to release the resource of TextToSpeech
        tts?.shutdown()
    }

    private fun speechText() {
        val editor = findViewById<EditText>(R.id.edit_text)
        editor.selectAll()
        // EditTextからテキストを取得
        val string = editor.text.toString()

        if (string.isNotEmpty()) {
            if (tts!!.isSpeaking) {
                tts?.stop()
            }
            setSpeechRate()
            setSpeechPitch()

            tts?.speak(string, TextToSpeech.QUEUE_FLUSH, null, "messageID")

            setTtsListener()
        }
    }

    // 読み上げのスピード
    private fun setSpeechRate() {
        tts?.setSpeechRate(1.0F)
    }

    // 読み上げのピッチ
    private fun setSpeechPitch() {
        tts?.setPitch(1.0F)
    }

    // 読み上げ始まりと終わりを取得
    private fun setTtsListener() {
        val listenerResult = tts?.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                Log.d("debug", "progress on Done $utteranceId")
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

    override fun onDestroy() {
        super.onDestroy()
        shutDown()
    }
}
