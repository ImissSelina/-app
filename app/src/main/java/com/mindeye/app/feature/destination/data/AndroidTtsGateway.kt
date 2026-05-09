package com.mindeye.app.feature.destination.data

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Android 原生 TTS 实现
 */
class AndroidTtsGateway(
    context: Context
) : TtsGateway, TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var pendingText: String? = null
    private var pendingDone: (() -> Unit)? = null
    private var initialized = false

    init {
        tts = TextToSpeech(appContext, this)
    }

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
        if (!initialized) return

        val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // fallback 到默认语言
            tts?.language = Locale.getDefault()
        }

        // 设置播报完成监听
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                pendingDone?.invoke()
                pendingDone = null
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                pendingDone?.invoke()
                pendingDone = null
            }
        })

        pendingText?.let {
            speak(it, pendingDone)
            pendingText = null
            pendingDone = null
        }
    }

    override fun speak(text: String, onDone: (() -> Unit)?) {
        if (!initialized) {
            pendingText = text
            pendingDone = onDone
            return
        }

        pendingDone = onDone
        val utteranceId = "utterance_${System.currentTimeMillis()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            @Suppress("DEPRECATION")
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
        }
    }

    override fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
