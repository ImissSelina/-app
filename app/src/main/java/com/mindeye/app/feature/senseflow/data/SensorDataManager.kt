package com.mindeye.app.feature.senseflow.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 传感器管理器
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val locationManager = context.getSystemService(LocationManager::class.java)
    
    // 最新传感器数据
    val currentSensorData = SensorData()
    
    // 协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())
    
    // 麦克风录音器
    private var mediaRecorder: MediaRecorder? = null
    private var decibelJob: Job? = null

    // 传感器监听器
    private val accelerationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            currentSensorData.acceleration = Math.sqrt((x*x + y*y + z*z).toDouble()).toFloat()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            currentSensorData.light = event.values[0]
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentSensorData.latitude = location.latitude
            currentSensorData.longitude = location.longitude
            currentSensorData.speed = location.speed
            currentSensorData.gpsAccuracy = location.accuracy
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    // 启动所有传感器
    fun startAllSensors() {
        // 加速度传感器
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(accelerationListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // 光线传感器
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // GPS定位
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000, // 2秒更新一次
                1f, // 1米更新一次
                locationListener
            )
        } catch (e: SecurityException) {
            // 权限被拒绝，后续处理
        }

        // 启动分贝采集
        startDecibelMonitoring()
    }

    // 停止所有传感器
    fun stopAllSensors() {
        sensorManager.unregisterListener(accelerationListener)
        sensorManager.unregisterListener(lightListener)
        locationManager.removeUpdates(locationListener)
        stopDecibelMonitoring()
    }

    // 启动分贝监测
    private fun startDecibelMonitoring() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null") // 不保存文件
                prepare()
                start()
            }

            decibelJob = coroutineScope.launch {
                while (true) {
                    delay(1000) // 每秒计算一次分贝
                    mediaRecorder?.let {
                        val amplitude = it.maxAmplitude
                        if (amplitude > 0) {
                            // 分贝计算公式
                            val decibel = 20 * Math.log10(amplitude.toDouble() / 1.0).toFloat()
                             currentSensorData.decibel = decibel
                         }
                     }
                 }
             }
         } catch (e: IOException) {
             e.printStackTrace()
         } catch (e: SecurityException) {
             // 权限被拒绝
         }
     }
     // 停止分贝监测
     private fun stopDecibelMonitoring() {
         decibelJob?.cancel()
         mediaRecorder?.apply {
             stop()
             release()
         }
         mediaRecorder = null
     }
 }
