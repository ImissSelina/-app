package com.mindeye.app.core.feedback

/**
 * 语音交互管理器接口
 * 抽象了 TTS（文本转语音）和 STT（语音转文本）功能，便于后续接入不同的 SDK
 */
interface SpeechManager {
    /**
     * 播报语音
     * @param text 要播报的文字
     * @param onComplete 播报完成后的回调
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null)

    /**
     * 开始语音识别
     * @param onResult 识别结果回调
     * @param onError 错误回调
     */
    fun startListening(onResult: (String) -> Unit, onError: ((String) -> Unit)? = null)

    /**
     * 停止语音识别
     */
    fun stopListening()
}
