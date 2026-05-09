package com.mindeye.app.feature.destination.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Android 原生语音识别实现
 */
class AndroidSpeechRecognizerGateway(
    private val context: Context
) : SpeechRecognizerGateway {

    private var listener: SpeechRecognizerListener? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var timeoutTimer: CountDownTimer? = null

    companion object {
        private const val LISTEN_TIMEOUT_MS = 15_000L
    }

    override fun setListener(listener: SpeechRecognizerListener) {
        this.listener = listener
    }

    override fun startListening() {
        if (!hasAudioPermission()) {
            listener?.onError("缺少录音权限")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener?.onError("当前设备不支持语音识别")
            return
        }

        ensureRecognizer()

        try {
            speechRecognizer?.startListening(buildIntent())
            startTimeout()
        } catch (e: Exception) {
            listener?.onError("启动语音识别失败：${e.message ?: "未知错误"}")
        }
    }

    override fun stopListening() {
        timeoutTimer?.cancel()
        timeoutTimer = null

        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    private fun ensureRecognizer() {
        if (speechRecognizer != null) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    // 可在这里提示“我在听”
                }

                override fun onBeginningOfSpeech() {
                    cancelTimeout()
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    // 等待结果
                }

                override fun onError(error: Int) {
                    cancelTimeout()
                    listener?.onError(mapError(error))
                }

                override fun onResults(results: Bundle?) {
                    cancelTimeout()
                    val matches = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                    val finalText = matches?.firstOrNull()?.trim().orEmpty()
                    if (finalText.isNotBlank()) {
                        listener?.onFinalResult(finalText)
                    } else {
                        listener?.onError("没有识别到有效内容")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()

                    if (partial.isNotBlank()) {
                        listener?.onPartialResult(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun buildIntent(): Intent {
        if (recognizerIntent == null) {
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            }
        }
        return recognizerIntent!!
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startTimeout() {
        cancelTimeout()
        timeoutTimer = object : CountDownTimer(LISTEN_TIMEOUT_MS, 1000L) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                listener?.onError("语音识别超时，请再说一遍目的地")
                stopListening()
            }
        }.start()
    }

    private fun cancelTimeout() {
        timeoutTimer?.cancel()
        timeoutTimer = null
    }

    private fun mapError(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "录音失败，请检查麦克风"
            SpeechRecognizer.ERROR_CLIENT -> "语音识别客户端错误"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "缺少录音权限"
            SpeechRecognizer.ERROR_NETWORK -> "网络错误，请检查网络连接"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时，请稍后重试"
            SpeechRecognizer.ERROR_NO_MATCH -> "没有听清，请再说一遍目的地"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "语音识别正在忙，请稍后重试"
            SpeechRecognizer.ERROR_SERVER -> "语音服务错误，请稍后重试"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "长时间没有听到内容，请再说一遍"
            else -> "语音识别失败，错误码：$errorCode"
        }
    }

    fun release() {
        cancelTimeout()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
        scope.cancel()
    }
}
