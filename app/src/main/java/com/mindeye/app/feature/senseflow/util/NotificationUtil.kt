package com.mindeye.app.feature.senseflow.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.mindeye.app.R

object NotificationUtil {
    const val CHANNEL_ID = "senseflow_service_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "环境感知服务",
                NotificationManager.IMPORTANCE_LOW // 低优先级，不弹出通知
            ).apply {
                description = "明心同行正在后台感知您的环境"
                setShowBadge(false)
                enableVibration(false)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildForegroundNotification(context: Context): Notification {
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("明心同行")
            .setContentText("正在感知环境，为您提供智能交互")
            .setSmallIcon(R.drawable.ic_senseflow) // 替换为新的app图标
            .setPriority(Notification.PRIORITY_LOW)
            .setOngoing(true) // 不可清除
            .build()
    }
}
