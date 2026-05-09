package com.mindeye.app.feature.destination.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindeye.app.feature.destination.model.DestinationFlowState

/**
 * 目的地输入界面
 * 适配视障用户：大字体、高对比度、简单的按钮
 */
@Composable
fun DestinationInputScreen(viewModel: DestinationInputViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. 顶部状态/提示区域
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = uiState.tipText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (uiState.recognizedText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "识别结果：“${uiState.recognizedText}”",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. 中间动画/状态展示区域
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState.flowState) {
                    DestinationFlowState.Listening -> {
                        // 这里后续可以放一个语音波纹动画
                        Text("正在聆听...", fontSize = 24.sp, color = Color.Red)
                    }
                    DestinationFlowState.Parsing -> {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    }
                    else -> {}
                }
            }

            // 3. 底部操作按钮区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.flowState) {
                    DestinationFlowState.Confirming -> {
                        Button(
                            onClick = { viewModel.onUserConfirm(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // 确认用绿色
                        ) {
                            Text("正确，开始出发", fontSize = 28.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.onUserConfirm(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text("不对，重新说", fontSize = 24.sp)
                        }
                    }

                    DestinationFlowState.Idle, DestinationFlowState.Cancelled, DestinationFlowState.Completed -> {
                        Button(
                            onClick = { viewModel.startFlow() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Text(
                                if (uiState.flowState == DestinationFlowState.Completed) "重新输入" else "点击说出目的地",
                                fontSize = 28.sp
                            )
                        }
                    }

                    else -> {
                        // 正在听或正在处理时，显示取消按钮
                        TextButton(
                            onClick = { viewModel.cancelFlow() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("取消", fontSize = 20.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
