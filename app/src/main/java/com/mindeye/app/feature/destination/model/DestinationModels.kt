package com.mindeye.app.feature.destination.model

/**
 * 目的地候选信息
 */
data class DestinationCandidate(
    val city: String? = null,
    val district: String? = null,
    val poiName: String? = null,
    val addressDetail: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * 目的地解析结果
 */
sealed class DestinationParseResult {
    data class Success(val candidate: DestinationCandidate) : DestinationParseResult()
    data class Ambiguous(val query: String, val options: List<DestinationCandidate>) : DestinationParseResult()
    data class NeedMoreInfo(val message: String) : DestinationParseResult()
    data class Failed(val message: String) : DestinationParseResult()
}

/**
 * 目的地输入流程状态
 */
enum class DestinationFlowState {
    Idle,               // 空闲
    Prompting,          // 询问中
    Listening,          // 正在听
    Parsing,            // 解析中
    Confirming,         // 确认中
    NeedClarification,  // 歧义待澄清
    Retrying,           // 重新输入
    Completed,          // 确认完成
    Cancelled           // 取消
}

/**
 * 目的地输入 UI 状态
 */
data class DestinationInputUiState(
    val flowState: DestinationFlowState = DestinationFlowState.Idle,
    val tipText: String = "等待输入目的地",
    val recognizedText: String = "",
    val parsedDestination: DestinationCandidate? = null
)
