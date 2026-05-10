package com.mindeye.app.feature.senseflow.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentDetector @Inject constructor(
    private val sensorDataManager: SensorDataManager
) {
    // 当前环境状态（可观察）
    private val _currentState = MutableStateFlow(EnvironmentState.UNKNOWN)
    val currentState: StateFlow<EnvironmentState> = _currentState.asStateFlow()

    // 状态防抖：连续3次检测到相同状态才切换
    private var pendingState: EnvironmentState = EnvironmentState.UNKNOWN
    private var pendingStateCount: Int = 0
    private val REQUIRED_CONSECUTIVE_CHECKS = 3

    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())
    private var detectionJob: Job? = null

    // 启动环境检测
    fun startDetection() {
        detectionJob = coroutineScope.launch {
            while (true) {
                delay(2000) // 每2秒检测一次
                val newState = detectEnvironmentState()
                updateStateWithDebounce(newState)
            }
        }
    }

    // 停止环境检测
    fun stopDetection() {
        detectionJob?.cancel()
    }

    // 核心状态判断逻辑
    private fun detectEnvironmentState(): EnvironmentState {
        val data = sensorDataManager.currentSensorData

        return when {
            // 通行中：加速度>2m/s² 且 GPS精度<20米
            data.acceleration > 2 && data.gpsAccuracy < 20 -> EnvironmentState.MOVING
            
            // 嘈杂：分贝>70dB
            data.decibel > 70 -> EnvironmentState.NOISY
            
            // 安静：分贝<40dB
            data.decibel < 40 -> EnvironmentState.QUIET
            
            // 社交：分贝在40-70dB之间
            data.decibel in 40f..70f -> EnvironmentState.SOCIAL
            
            // 室外：GPS精度<10米
            data.gpsAccuracy < 10 -> EnvironmentState.OUTDOOR
            
            // 室内：GPS精度>50米
            data.gpsAccuracy > 50 -> EnvironmentState.INDOOR
            
            else -> EnvironmentState.UNKNOWN
        }
    }

    // 状态防抖机制
    private fun updateStateWithDebounce(newState: EnvironmentState) {
        if (newState == pendingState) {
            pendingStateCount++
            if (pendingStateCount >= REQUIRED_CONSECUTIVE_CHECKS) {
                if (newState != _currentState.value) {
                    _currentState.value = newState
                }
                pendingStateCount = 0
            }
        } else {
            pendingState = newState
            pendingStateCount = 1
        }
    }
}
