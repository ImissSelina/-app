package com.mindeye.app.feature.senseflow.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SenseFlowModule {
    // 这里暂时不需要额外绑定，SensorDataManager已经用@Singleton标记
}
