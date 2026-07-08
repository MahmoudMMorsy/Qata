package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel

data class CyberTool(val id: String, val arabicName: String, val icon: ImageVector, val tabIndex: Int)

@Composable
fun WorkspaceControlTabs(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel,
    selectedTabIdx: Int,
    onTabSelected: (Int) -> Unit,
    workspaceMode: Int
) {
    var showSpeedSlider by remember { mutableStateOf(false) }

    val tools = when (workspaceMode) {
        0 -> listOf(
            CyberTool("CUT", "القص والتقطيع", Icons.Default.ContentCut, 0),
            CyberTool("SPEED", "سرعة التشغيل", Icons.Default.Speed, -1),
            CyberTool("AUDIO", "موزع الصوت", Icons.Default.MusicNote, 3)
        )
        1 -> listOf(
            CyberTool("FX", "مساعد الذكاء", Icons.Default.AutoAwesome, 4),
            CyberTool("TEXT", "النصوص والترجمة", Icons.Default.TextFields, 2)
        )
        else -> listOf(
            CyberTool("FILTER", "فلاتر سينمائية", Icons.Default.ColorLens, 1),
            CyberTool("TRANS", "الضغط والتنسيق", Icons.Default.Tune, 5)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Speed slider card overlay when SPEED is clicked
        AnimatedVisibility(
            visible = showSpeedSlider,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سرعة تشغيل المعاينة والتايملاين:",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.playbackSpeed}x",
                            color = NeonPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 4.0f).forEach { speed ->
                            val isCurrentSpeed = uiState.playbackSpeed == speed
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrentSpeed) NeonPink else SurfaceLight)
                                    .border(BorderStroke(1.dp, if (isCurrentSpeed) NeonPink else SurfaceLight.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.updatePlaybackSpeed(speed) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${speed}x",
                                    color = if (isCurrentSpeed) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cyberpunk 7-tool Neon Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .border(BorderStroke(1.dp, Color(0xFF161B29)), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tools.forEach { tool ->
                val label = tool.arabicName
                val icon = tool.icon
                val tabIdx = tool.tabIndex
                val isSelected = if (tabIdx == -1) showSpeedSlider else (selectedTabIdx == tabIdx && !showSpeedSlider)
                val color = if (isSelected) NeonPink else TextSecondary

                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (tabIdx == -1) {
                                showSpeedSlider = !showSpeedSlider
                            } else {
                                showSpeedSlider = false
                                onTabSelected(tabIdx)
                            }
                        }
                        .testTag("cyber_tool_${tool.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 0.dp,
                                shape = CircleShape,
                                ambientColor = NeonPink,
                                spotColor = NeonPink
                            )
                            .border(BorderStroke(if (isSelected) 2.dp else 1.dp, color.copy(alpha = 0.8f)), CircleShape)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = label,
                        color = color,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Selected Tab Content Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SurfaceSlate,
                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .border(
                    BorderStroke(1.dp, Color(0xFF161B29)),
                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .padding(16.dp)
        ) {
            when (selectedTabIdx) {
                0 -> SplitControlView(uiState, viewModel)
                1 -> ColorGradingControlView(uiState, viewModel)
                2 -> TitlesSubtitlesControlView(uiState, viewModel)
                3 -> AudioMixerControlView(uiState, viewModel)
                4 -> LocalAiControlView(uiState, viewModel)
                5 -> CompressionControlView(uiState, viewModel)
            }
        }
    }
}
