package com.mindeye.app.feature.senseflow.data

data class SensorData(
    val decibel: Float = 0f, // 环境分贝值
    val acceleration: Float = 0f, // 加速度
    val light: Float = 0f, // 光照强度
    val latitude: Double = 0.0, // 纬度
    val longitude: Double = 0.0, // 经度
    val speed: Float = 0f, // 速度(m/s)
    val gpsAccuracy: Float = 0f // GPS精度
)
