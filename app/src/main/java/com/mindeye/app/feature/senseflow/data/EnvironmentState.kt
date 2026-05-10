package com.mindeye.app.feature.senseflow.data

// 环境状态枚举
enum class EnvironmentState {
    UNKNOWN,    // 未知
    QUIET,      // 安静
    NOISY,      // 嘈杂
    MOVING,     // 通行中
    INDOOR,     // 室内
    OUTDOOR,    // 室外
    SOCIAL      // 社交场景
}

// 交互策略数据类
data class InteractionStrategy(
    val speechVolume: Float = 0.5f,    // 语音音量(0.0-1.0)
    val vibrationIntensity: Int = 150, // 震动强度(0-255)
    val promptFrequency: Int = 10,     // 提示频率(秒)
    val enableVoicePrompt: Boolean = true, // 是否开启语音提示
    val enableVibration: Boolean = true   // 是否开启震动
)
