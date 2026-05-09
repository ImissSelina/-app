package com.mindeye.app.feature.destination.data

import com.mindeye.app.feature.destination.model.DestinationParseResult

/**
 * 语音识别能力抽象层
 */
interface SpeechRecognizerGateway {
    fun startListening()
    fun stopListening()
    fun setListener(listener: SpeechRecognizerListener)
}

/**
 * 语音识别监听回调
 */
interface SpeechRecognizerListener {
    fun onPartialResult(text: String)
    fun onFinalResult(text: String)
    fun onError(errorMessage: String)
}

/**
 * 语音播报能力抽象层
 */
interface TtsGateway {
    fun speak(text: String, onDone: (() -> Unit)? = null)
    fun stop()
}

/**
 * 目的地解析能力抽象层
 */
interface DestinationParser {
    fun parse(input: String): DestinationParseResult
}
