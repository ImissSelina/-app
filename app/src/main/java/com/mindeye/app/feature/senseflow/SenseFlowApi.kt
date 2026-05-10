package com.mindeye.app.feature.senseflow

import com.mindeye.app.feature.senseflow.data.EnvironmentState
import com.mindeye.app.feature.senseflow.data.InteractionStrategy
import kotlinx.coroutines.flow.Flow

interface SenseFlowApi {
    // 获取当前环境状态
    fun getCurrentEnvironmentState(): EnvironmentState

    // 获取当前交互策略
    fun getCurrentInteractionStrategy(): InteractionStrategy

    // 观察环境状态变化
    fun observeEnvironmentState(): Flow<EnvironmentState>

    // 观察交互策略变化
    fun observeInteractionStrategy(): Flow<InteractionStrategy>

    // 强制覆盖策略（SOS模块用）
    fun forceOverrideStrategy(strategy: InteractionStrategy)

    // 恢复默认策略
    fun restoreDefaultStrategy()
}
