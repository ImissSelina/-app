package com.mindeye.app.feature.destination.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.mindeye.app.feature.destination.data.*
import javax.inject.Singleton

/**
 * 目的地输入模块的 Hilt 依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object DestinationModule {

    @Provides
    @Singleton
    fun provideSpeechRecognizerGateway(
        @ApplicationContext context: Context
    ): SpeechRecognizerGateway = AndroidSpeechRecognizerGateway(context)

    @Provides
    @Singleton
    fun provideTtsGateway(
        @ApplicationContext context: Context
    ): TtsGateway = AndroidTtsGateway(context)

    @Provides
    @Singleton
    fun provideDestinationParser(
        @ApplicationContext context: Context
    ): DestinationParser = AmapDestinationParser(context)
}
