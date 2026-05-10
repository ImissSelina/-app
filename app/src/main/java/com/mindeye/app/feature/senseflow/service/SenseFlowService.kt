package com.mindeye.app.feature.senseflow.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.mindeye.app.feature.senseflow.data.EnvironmentDetector
import com.mindeye.app.feature.senseflow.data.SensorDataManager
import com.mindeye.app.feature.senseflow.util.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SenseFlowService : Service() {
    private val binder = LocalBinder()

    // 注入传感器数据管理器
    @Inject
    lateinit var sensorDataManager: SensorDataManager

    // 注入环境状态检测器
    @Inject
    lateinit var environmentDetector: EnvironmentDetector

    inner class LocalBinder : Binder() {
        fun getService(): SenseFlowService = this@SenseFlowService
    }

    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道（Android 8.0+ 强制要求）
        NotificationUtil.createNotificationChannel(this)
        // 启动前台服务，显示常驻通知
        startForeground(
            NotificationUtil.NOTIFICATION_ID,
            NotificationUtil.buildForegroundNotification(this)
        )
        // 启动所有传感器，开始采集数据
        sensorDataManager.startAllSensors()
        // 启动环境状态检测循环
        environmentDetector.startDetection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 服务被系统杀死后自动重启
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止所有传感器，释放硬件资源
        sensorDataManager.stopAllSensors()
        // 停止环境状态检测循环
        environmentDetector.stopDetection()
    }
}
