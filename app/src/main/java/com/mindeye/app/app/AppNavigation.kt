package com.mindeye.app.app

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindeye.app.feature.destination.ui.DestinationInputScreen
import com.mindeye.app.feature.destination.ui.DestinationInputViewModel
import com.mindeye.app.feature.home.ui.HomeScreen

/**
 * 全局导航配置
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 首页路由
        composable("home") {
            HomeScreen(
                onNavigateToDestination = {
                    navController.navigate("destination_input")
                }
            )
        }

        // 目的地输入页面路由
        composable("destination_input") {
            val viewModel: DestinationInputViewModel = hiltViewModel()
            DestinationInputScreen(viewModel = viewModel)
        }
    }
}
