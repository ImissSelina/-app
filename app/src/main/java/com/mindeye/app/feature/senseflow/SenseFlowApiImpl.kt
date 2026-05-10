package com.mindeye.app.feature.senseflow

import com.mindeye.app.feature.senseflow.data.EnvironmentDetector
import com.mindeye.app.feature.senseflow.data.EnvironmentState
import com.mindeye.app.feature.senseflow.data.InteractionStrategy
import com.mindeye.app.feature.senseflow.data.InteractionStrategyController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SenseFlowApiImpl @Inject constructor(
    private val environmentDetector: EnvironmentDetector,
    private val strategyController: InteractionStrategyController
) : SenseFlowApi {

    private val _overrideStrategy = MutableStateFlow<InteractionStrategy?>(null)

    override fun getCurrentEnvironmentState(): EnvironmentState {
        return environmentDetector.currentState.value
    }

    override fun getCurrentInteractionStrategy(): InteractionStrategy {
        return _overrideStrategy.value ?: strategyController.getCurrentStrategy()
    }

    override fun observeEnvironmentState(): Flow<EnvironmentState> {
        return environmentDetector.currentState
    }

    override fun observeInteractionStrategy(): Flow<InteractionStrategy> {
        return combine(strategyController.strategyFlow, _overrideStrategy) { default, override ->
            override ?: default
        }
    }

    override fun forceOverrideStrategy(strategy: InteractionStrategy) {
        _overrideStrategy.value = strategy
    }

    override fun restoreDefaultStrategy() {
        _overrideStrategy.value = null
    }
}
