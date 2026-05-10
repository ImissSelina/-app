package com.mindeye.app.feature.senseflow.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SenseFlowModule {
    @Binds
    abstract fun bindSenseFlowApi(impl: SenseFlowApiImpl): SenseFlowApi
}
