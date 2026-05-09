package com.mindeye.app.feature.destination.ui

import androidx.lifecycle.ViewModel
import com.mindeye.app.feature.destination.data.*
import com.mindeye.app.feature.destination.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 目的地输入 ViewModel
 * 负责管理交互流程状态机，协调语音识别、播报和目的地解析
 */
class DestinationInputViewModel(
    private val speechRecognizer: SpeechRecognizerGateway,
    private val tts: TtsGateway,
    private val parser: DestinationParser
) : ViewModel(), SpeechRecognizerListener {

    private val _uiState = MutableStateFlow(DestinationInputUiState())
    val uiState: StateFlow<DestinationInputUiState> = _uiState

    init {
        speechRecognizer.setListener(this)
    }

    /**
     * 启动整个询问流程
     */
    fun startFlow() {
        val prompt = "请告诉我你要去哪里。你可以直接说地点名称或地址。"
        _uiState.update {
            it.copy(
                flowState = DestinationFlowState.Prompting,
                tipText = prompt
            )
        }
        tts.speak(prompt) {
            startListening()
        }
    }

    /**
     * 开始语音监听
     */
    fun startListening() {
        _uiState.update { it.copy(flowState = DestinationFlowState.Listening) }
        speechRecognizer.startListening()
    }

    // --- SpeechRecognizerListener 实现 ---

    override fun onPartialResult(text: String) {
        _uiState.update { it.copy(recognizedText = text) }
    }

    override fun onFinalResult(text: String) {
        _uiState.update {
            it.copy(
                flowState = DestinationFlowState.Parsing,
                recognizedText = text
            )
        }
        processRecognizedText(text)
    }

    override fun onError(errorMessage: String) {
        handleError("识别出错了，请稍后再试。")
    }

    /**
     * 处理识别到的最终文本
     */
    private fun processRecognizedText(text: String) {
        when (val result = parser.parse(text)) {
            is DestinationParseResult.Success -> {
                val candidate = result.candidate
                val confirmText = buildConfirmText(candidate)
                _uiState.update {
                    it.copy(
                        flowState = DestinationFlowState.Confirming,
                        parsedDestination = candidate,
                        tipText = confirmText
                    )
                }
                tts.speak(confirmText)
            }

            is DestinationParseResult.Ambiguous -> {
                val msg = "你说的“${result.query}”有多个地点，请告诉我更具体的信息。"
                _uiState.update {
                    it.copy(
                        flowState = DestinationFlowState.NeedClarification,
                        tipText = msg
                    )
                }
                tts.speak(msg) { startListening() }
            }

            is DestinationParseResult.NeedMoreInfo -> {
                _uiState.update {
                    it.copy(
                        flowState = DestinationFlowState.Retrying,
                        tipText = result.message
                    )
                }
                tts.speak(result.message) { startListening() }
            }

            is DestinationParseResult.Failed -> {
                val msg = result.message.ifBlank { "不好意思，我没听清，请再说一遍目的地好吗？" }
                handleError(msg)
            }
        }
    }

    /**
     * 处理用户确认/取消操作
     */
    fun onUserConfirm(confirm: Boolean) {
        if (confirm) {
            val destination = _uiState.value.parsedDestination
            _uiState.update { it.copy(flowState = DestinationFlowState.Completed) }
            tts.speak("好的，已确认目的地：${destination?.poiName ?: "目的地"}。")
        } else {
            _uiState.update {
                it.copy(
                    flowState = DestinationFlowState.Retrying,
                    tipText = "好的，请重新说出目的地。"
                )
            }
            tts.speak("好的，请重新说出目的地。") { startListening() }
        }
    }

    /**
     * 取消流程
     */
    fun cancelFlow() {
        speechRecognizer.stopListening()
        _uiState.update {
            it.copy(
                flowState = DestinationFlowState.Cancelled,
                tipText = "已取消本次出行目的地输入。"
            )
        }
        tts.speak("已取消本次出行目的地输入。")
    }

    private fun handleError(message: String) {
        _uiState.update {
            it.copy(
                flowState = DestinationFlowState.Retrying,
                tipText = message
            )
        }
        tts.speak(message) { startListening() }
    }

    private fun buildConfirmText(candidate: DestinationCandidate): String {
        val name = candidate.poiName ?: "目的地"
        val address = candidate.addressDetail?.let { "，地址是$it" } ?: ""
        return "我理解你要去的是“$name”$address，对吗？"
    }
}
