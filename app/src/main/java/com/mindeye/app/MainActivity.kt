package com.mindeye.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mindeye.app.app.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * 应用主入口 Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 使用应用主题
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 加载导航图
                    AppNavigation()
                }
            }
        }
        // 启动随境感知服务
val serviceIntent = Intent(this, SenseFlowService::class.java)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(serviceIntent)
} else {
    startService(serviceIntent)
}
    }
}
