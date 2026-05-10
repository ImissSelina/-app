package com.mindeye.app.feature.senseflow.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionStrategyController @Inject constructor(
    private val environmentDetector: EnvironmentDetector
) {
    // 默认策略配置
    private val defaultStrategies = mapOf(
        EnvironmentState.UNKNOWN to InteractionStrategy(0.5f, 150, 10, true, true),
        EnvironmentState.QUIET to InteractionStrategy(0.3f, 100, 10, true, true),
        EnvironmentState.NOISY to InteractionStrategy(1.0f, 200, 5, true, true),
        EnvironmentState.MOVING to InteractionStrategy(0.8f, 255, 3, true, true),
        EnvironmentState.INDOOR to InteractionStrategy(0.5f, 150, 10, true, true),
        EnvironmentState.OUTDOOR to InteractionStrategy(0.6f, 180, 8, true, true),
        EnvironmentState.SOCIAL to InteractionStrategy(0.5f, 150, 15, false, true)
    )

    // 观察策略变化
    val strategyFlow: Flow<InteractionStrategy> = 
        environmentDetector.currentState.map { state ->
            defaultStrategies[state] ?: defaultStrategies[EnvironmentState.UNKNOWN]!!
        }

    // 获取当前策略
    fun getCurrentStrategy(): InteractionStrategy {
        val currentState = environmentDetector.currentState.value
        return defaultStrategies[currentState] ?: defaultStrategies[EnvironmentState.UNKNOWN]!!
    }
}
