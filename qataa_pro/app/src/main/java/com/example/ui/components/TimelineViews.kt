package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.media.*
import com.example.ui.theme.*
import com.example.viewmodel.*
@Composable
fun TimelineTracks(
    uiState: EditorUiState,
    onSeek: (Long) -> Unit,
    onStartChange: (Long) -> Unit,
    onEndChange: (Long) -> Unit
) {
    val duration = uiState.currentMedia?.durationMs ?: 120000L
    val startRatio = uiState.selectionStartMs.toFloat() / duration
    val endRatio = uiState.selectionEndMs.toFloat() / duration
    val playheadRatio = uiState.playheadMs.toFloat() / duration

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مخطط التايملاين (مسارات المونتاج والقص)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            // Selection range info
            Text(
                text = "النطاق المحدد: ${formatTime(uiState.selectionStartMs)} ⟷ ${formatTime(uiState.selectionEndMs)}  [${formatTime(uiState.selectionEndMs - uiState.selectionStartMs)}]",
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Entire tracks box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceSlate, RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, SurfaceLight), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Time code ticks ruler
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .drawBehind {
                            val w = size.width
                            val stepsCount = 10
                            for (i in 0..stepsCount) {
                                val x = w * (i.toFloat() / stepsCount)
                                val tickHeight = if (i % 2 == 0) 14f else 8f
                                drawLine(
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    start = Offset(x, size.height - tickHeight),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                )

                // 2. VIDEO TRACK (V1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(SurfaceLight, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    // Highlight selected region on the track
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(endRatio - startRatio)
                            .offset(x = (16.dp + (startRatio * 100f).dp)) // approximate visual layout offset simulation
                            .background(TrackVideoColor.copy(alpha = 0.75f))
                            .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(4.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoCameraFront,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مسار الفيديو: V1",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2b. SECONDARY VIDEO TRACK (V2 Overlay)
                if (uiState.isVideoTrack2Enabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(SurfaceLight, RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .alpha(if (uiState.isVideoTrack2Visible) 1f else 0.4f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.4f) // Simulated overlay clip
                                .offset(x = 60.dp)
                                .background(NeonPurple.copy(alpha = 0.6f))
                                .border(BorderStroke(1.dp, if (uiState.isVideoTrack2Locked) Color.Red else NeonPurple), RoundedCornerShape(4.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.isVideoTrack2Locked) Icons.Default.Lock else Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "مسار الطبقات: V2",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. AUDIO TRACK (A1 Original Sound)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(SurfaceLight, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    // Highlight selected audio region
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(endRatio - startRatio)
                            .offset(x = (16.dp + (startRatio * 100f).dp))
                            .background(TrackAudioColor.copy(alpha = 0.75f))
                            .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(4.dp))
                    ) {
                        // Drawing waveform lines inside audio track
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val w = size.width
                            val h = size.height
                            val lines = 30
                            for (i in 0 until lines) {
                                val x = w * (i.toFloat() / lines)
                                val lineH = (Math.sin(i.toDouble() * 0.8) * (h * 0.3) + (h * 0.4)).toFloat()
                                drawLine(
                                    color = if (uiState.isOriginalAudioMuted) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.3f),
                                    start = Offset(x, (h - lineH) / 2f),
                                    end = Offset(x, (h + lineH) / 2f),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (uiState.isOriginalAudioMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (uiState.isOriginalAudioMuted) TextSecondary else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "مسار الصوت: A1 " + if (uiState.isOriginalAudioMuted) "(مكتوم 🔕)" else "",
                                    color = if (uiState.isOriginalAudioMuted) TextSecondary else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 5. TERTIARY AUDIO TRACK (A3 Voiceovers/FX)
                if (uiState.isAudioTrack3Enabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(SurfaceLight, RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            for (i in 0 until 40) {
                                val x = w * (i.toFloat() / 40)
                                val lineH = (Math.random() * h * 0.6).toFloat()
                                drawLine(
                                    color = SuccessGreen.copy(alpha = if (uiState.isAudioTrack3Muted) 0.1f else 0.4f),
                                    start = Offset(x, (h - lineH) / 2f),
                                    end = Offset(x, (h + lineH) / 2f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (uiState.isAudioTrack3Muted) Icons.Default.VolumeOff else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.audioTrack3Name + if (uiState.isAudioTrack3Muted) " (مكتوم)" else "",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Loudness: ${(uiState.audioTrack3Volume * 100).toInt()}%",
                                    color = SuccessGreen,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 4. SECONDARY AUDIO TRACK (A2 Soundtrack Track)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(
                            if (uiState.soundtrack != "NONE") SurfaceLight else Color.Black.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            BorderStroke(
                                1.dp,
                                if (uiState.soundtrack != "NONE") NeonCyan.copy(alpha = 0.4f) else Color.Transparent
                            ),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    if (uiState.soundtrack != "NONE") {
                        // Drawing secondary waveform
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val lines = 36
                            for (i in 0 until lines) {
                                val x = w * (i.toFloat() / lines)
                                val lineH = (Math.cos(i.toDouble() * 1.1) * (h * 0.25) + (h * 0.35)).toFloat()
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.35f),
                                    start = Offset(x, (h - lineH) / 2f),
                                    end = Offset(x, (h + lineH) / 2f),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val soundtrackLabel = when (uiState.soundtrack) {
                                    "CINEMATIC_EPIC" -> "موسيقى: سينمائي ملحمي 🎵"
                                    "LOFI_CHILL" -> "موسيقى: لو فاي هادئ ☕"
                                    "VLOG_HAPPY" -> "موسيقى: فلوق مبهج ✨"
                                    else -> "مسار الصوت الثانوي: A2"
                                }
                                Text(
                                    text = soundtrackLabel,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Volume bubble indicator
                            Box(
                                modifier = Modifier
                                    .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "مستوى الصوت: ${(uiState.soundtrackVolume * 100).toInt()}%",
                                    color = NeonCyan,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Empty A2 Track placeholder
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مسار الموسيقى والمؤثرات الصوتية: A2 (فارغ ⊕)",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Draggable range sliders & Playhead layers overlay
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .testTag("timeline_gesture_area")
            ) {
                val w = size.width

                // Draw orange playhead line
                val playheadX = w * playheadRatio
                drawLine(
                    color = PremiereOrange,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 2.dp.toPx()
                )

                // Playhead top marker diamond
                val markerPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(playheadX - 6.dp.toPx(), 0f)
                    lineTo(playheadX + 6.dp.toPx(), 0f)
                    lineTo(playheadX, 8.dp.toPx())
                    close()
                }
                drawPath(path = markerPath, color = PremiereOrange)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Range Trim sliders
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceSlate, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.West,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "نقطة البداية (Start Cut): ${formatTime(uiState.selectionStartMs)}",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Slider(
                    value = startRatio,
                    onValueChange = { onStartChange((it * duration).toLong()) },
                    modifier = Modifier
                        .width(160.dp)
                        .height(18.dp)
                        .testTag("selection_start_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan.copy(alpha = 0.5f),
                        inactiveTrackColor = SurfaceLight
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.East,
                    contentDescription = null,
                    tint = PremiereOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "نقطة النهاية (End Cut): ${formatTime(uiState.selectionEndMs)}",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Slider(
                    value = endRatio,
                    onValueChange = { onEndChange((it * duration).toLong()) },
                    modifier = Modifier
                        .width(160.dp)
                        .height(18.dp)
                        .testTag("selection_end_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = PremiereOrange,
                        activeTrackColor = PremiereOrange.copy(alpha = 0.5f),
                        inactiveTrackColor = SurfaceLight
                    )
                )
            }
        }
    }
}

data class CyberTool(val id: String, val arabicName: String, val icon: ImageVector, val tabIndex: Int)

/**
 * 4. Control Panels Tabbed Container
 */
