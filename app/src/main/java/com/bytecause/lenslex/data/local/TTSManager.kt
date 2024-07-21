package com.bytecause.lenslex.data.local

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*


class TTSManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    fun speak(text: String, langCode: String): Boolean {
        return if (isInitialized) {
            val result = tts.setLanguage(Locale(langCode))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                false
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
                true
            }
        } else {
            Log.e("TTS", "TTS is not initialized")
            true
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

