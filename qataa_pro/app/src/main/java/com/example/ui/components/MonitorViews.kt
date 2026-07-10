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
fun PreviewMonitorWithCinemaScopes(
    uiState: EditorUiState,
    cinemaMode: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val duration = uiState.currentMedia?.durationMs ?: 120000L
    val progress = if (duration > 0) uiState.playheadMs.toFloat() / duration else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "monitor_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    // Build the dynamic ColorMatrix to show live color grading in the monitor!
    val filterMatrix = uiState.selectedFilter.matrix
    val liveColorMatrix = remember(filterMatrix, uiState.brightness, uiState.contrast, uiState.saturation, uiState.temperature, uiState.isAiColorizationEnabled, uiState.aiColorizationIntensity, uiState.aiColorizationVibrancy) {
        val result = if (uiState.isAiColorizationEnabled) {
            // Convert to base grayscale representation of B&W historic photo/video
            val bw = ColorMatrix().apply { setToSaturation(0f) }
            val colorize = ColorMatrix().apply {
                // Infuse beautiful neural color tones based on sliders
                val intense = uiState.aiColorizationIntensity
                val vibe = uiState.aiColorizationVibrancy
                set(0, 0, 1.15f * intense)
                set(1, 1, 1.0f * vibe)
                set(2, 2, 0.85f * intense)
                set(0, 4, 25f * intense)
                set(1, 4, 10f * vibe)
                set(2, 4, -5f * intense)
            }
            bw.apply { timesAssign(colorize) }
        } else {
            ColorMatrix(filterMatrix)
        }
        val brightnessMatrix = ColorMatrix().apply {
            setToScale(1f, 1f, 1f, 1f)
            set(0, 4, uiState.brightness * 255f)
            set(1, 4, uiState.brightness * 255f)
            set(2, 4, uiState.brightness * 255f)
        }
        val saturationMatrix = ColorMatrix().apply {
            setToSaturation(uiState.saturation)
        }
        val contrastMatrix = ColorMatrix().apply {
            val scale = uiState.contrast
            val translate = (-.5f * scale + .5f) * 255f
            set(0, 0, scale)
            set(1, 1, scale)
            set(2, 2, scale)
            set(0, 4, translate)
            set(1, 4, translate)
            set(2, 4, translate)
        }
        val tempMatrix = ColorMatrix().apply {
            set(0, 4, uiState.temperature * 50f)
            set(2, 4, -uiState.temperature * 50f)
        }
        result.apply {
            timesAssign(brightnessMatrix)
            timesAssign(contrastMatrix)
            timesAssign(saturationMatrix)
            timesAssign(tempMatrix)
        }
    }

    // Cinema Ambient Glow effect modifier
    val borderGlow = if (cinemaMode) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = NeonCyan,
                spotColor = TrackVideoColor
            )
            .border(
                BorderStroke(2.dp, Brush.horizontalGradient(listOf(NeonCyan, TrackVideoColor))),
                RoundedCornerShape(12.dp)
            )
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(BorderStroke(1.dp, SurfaceLight), RoundedCornerShape(12.dp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(12.dp),
        modifier = borderGlow
    ) {
        Column {
            // Monitor Header info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (uiState.isPlaying) Color.Red else TextSecondary, CircleShape)
                    )
                    Text(
                        text = if (uiState.isPlaying) "بث سينمائي مباشر (نشط)" else "مراقبة الألوان والمعاينة",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cinemaMode) {
                    Text(
                        text = "وضع السينما ومحلل الإشارة نشط 🎚️",
                        color = PremiereOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = if (uiState.isAiUpscaleEnabled) "دقة عصبية فائقة: 8K AI (مفعّل)" else "دقة العرض: 4K UHD 60fps",
                        color = if (uiState.isAiUpscaleEnabled) NeonCyan else Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (uiState.isAiUpscaleEnabled) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // The Viewport showing our custom live graded vector graphics representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF07090C))
                    .testTag("preview_monitor"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF020205), Color(0xFF090714), Color(0xFF140D26))
                        ),
                        size = size
                    )

                    // Calculate active rendering width based on aspect format
                    val aspectWidth = when (uiState.aspectFormat) {
                        "9:16" -> h * (9f / 16f)
                        "1:1" -> h * (1f / 1f)
                        "4:3" -> h * (4f / 3f)
                        else -> w // 16:9 full width
                    }
                    val startX = ((w - aspectWidth) / 2f).coerceAtLeast(0f)
                    val endX = (startX + aspectWidth).coerceAtMost(w)

                    clipRect {
                        // Clip the drawing paths inside the active aspect ratio frame!
                        clipRect(left = startX, right = endX) {
                            val horizonY = h * 0.65f

                            // 1. Synthwave Glowing Sun
                            val sunRadius = 40.dp.toPx()
                            val sunCenter = Offset(w * 0.5f, horizonY - 10.dp.toPx())
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(NeonPink, Color(0xFFFFD600))
                                ),
                                radius = sunRadius,
                                center = sunCenter,
                                colorFilter = ColorFilter.colorMatrix(liveColorMatrix)
                            )
                            // Draw horizontal scanlines through the sun (retro synthwave style)
                            val scanlineHeight = 3.dp.toPx()
                            val scanlineSpacing = 8.dp.toPx()
                            var lineY = sunCenter.y - sunRadius
                            while (lineY < sunCenter.y + sunRadius) {
                                // Only draw slit lines in the lower half of the sun, increasing in thickness
                                if (lineY > sunCenter.y - 10.dp.toPx()) {
                                    val factor = (lineY - (sunCenter.y - 10.dp.toPx())) / sunRadius
                                    val thickness = scanlineHeight * (0.8f + factor * 1.5f)
                                    drawRect(
                                        color = Color(0xFF090714),
                                        topLeft = Offset(sunCenter.x - sunRadius, lineY),
                                        size = Size(sunRadius * 2f, thickness)
                                    )
                                }
                                lineY += scanlineSpacing
                            }

                            // 2. Cyberpunk Skyline / Skyscrapers on Horizon
                            val buildingColors = listOf(Color(0xFF2C0F4D), Color(0xFF180A2B), Color(0xFF3B1566))
                            val buildings = listOf(
                                Triple(0.12f, 0.25f, 0.35f), // startX_ratio, width_ratio, height_ratio
                                Triple(0.28f, 0.18f, 0.45f),
                                Triple(0.42f, 0.22f, 0.28f),
                                Triple(0.6f, 0.2f, 0.4f),
                                Triple(0.75f, 0.15f, 0.32f)
                            )
                            buildings.forEachIndexed { i, b ->
                                val bWidth = w * b.second
                                val bHeight = h * b.third
                                val bX = startX + (endX - startX) * b.first - bWidth / 2f
                                val bY = horizonY - bHeight
                                drawRect(
                                    color = buildingColors[i % buildingColors.size],
                                    topLeft = Offset(bX, bY),
                                    size = Size(bWidth, bHeight),
                                    colorFilter = ColorFilter.colorMatrix(liveColorMatrix)
                                )
                                // Draw some neon window dots
                                val dotSize = 2.dp.toPx()
                                for (row in 1..4) {
                                    for (col in 1..3) {
                                        if ((row + col + i) % 3 != 0) {
                                            drawRect(
                                                color = if (i % 2 == 0) NeonCyan else NeonPink,
                                                topLeft = Offset(bX + bWidth * (col / 4f), bY + bHeight * (row / 6f)),
                                                size = Size(dotSize, dotSize)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Cyberpunk Ground Perspective Grid
                            // Horizontal ground perspective lines
                            val gridLines = 8
                            for (gridI in 0..gridLines) {
                                val ratio = gridI.toFloat() / gridLines
                                val lineY = horizonY + (h - horizonY) * (ratio * ratio) // non-linear for depth
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.35f + ratio * 0.45f),
                                    start = Offset(startX, lineY),
                                    end = Offset(endX, lineY),
                                    strokeWidth = (1f + ratio * 1.5f).dp.toPx()
                                )
                            }
                            // Converging perspective lines from center horizon
                            val verticalGridLines = 10
                            for (gridI in 0..verticalGridLines) {
                                val ratio = gridI.toFloat() / verticalGridLines
                                val bottomX = startX + (endX - startX) * ratio
                                val topX = startX + (endX - startX) * (0.5f + (ratio - 0.5f) * 0.15f) // converge toward center
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.4f),
                                    start = Offset(topX, horizonY),
                                    end = Offset(bottomX, h),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // 4. Glowing flying cyberpunk vehicle/streak
                            val animOffset = waveOffset / 360f // 0.0 to 1.0
                            val carX = startX + (endX - startX) * animOffset
                            val carY = horizonY - 30.dp.toPx() - (Math.sin(animOffset * Math.PI * 2) * 15f).toFloat()
                            // Car glowing streak trail
                            drawCircle(
                                color = NeonPink,
                                radius = 3.dp.toPx(),
                                center = Offset(carX, carY)
                            )
                            drawLine(
                                color = NeonPink.copy(alpha = 0.6f),
                                start = Offset((carX - 25.dp.toPx()).coerceAtLeast(startX), carY),
                                end = Offset(carX, carY),
                                strokeWidth = 3.dp.toPx()
                            )
                            // Car outline
                            drawRect(
                                color = NeonCyan,
                                topLeft = Offset(carX - 8.dp.toPx(), carY - 3.dp.toPx()),
                                size = Size(10.dp.toPx(), 4.dp.toPx())
                            )

                            // 5. Playback tracker line overlay (current playback scrubber position)
                            val playheadX = startX + (endX - startX) * progress
                            drawLine(
                                color = Color.Red.copy(alpha = 0.9f),
                                start = Offset(playheadX, 0f),
                                end = Offset(playheadX, h),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawCircle(
                                color = Color.Red,
                                radius = 6.dp.toPx(),
                                center = Offset(playheadX, 8.dp.toPx())
                            )
                        }

                        // Draw cropping overlay / letterboxing dark bars for pro camera vibe
                        if (startX > 0f) {
                            // Left dark pillar
                            drawRect(
                                color = Color.Black.copy(alpha = 0.75f),
                                topLeft = Offset(0f, 0f),
                                size = Size(startX, h)
                            )
                            // Right dark pillar
                            drawRect(
                                color = Color.Black.copy(alpha = 0.75f),
                                topLeft = Offset(endX, 0f),
                                size = Size(w - endX, h)
                            )
                            // Neon cyan cropping guides
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.5f),
                                start = Offset(startX, 0f),
                                end = Offset(startX, h),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.5f),
                                start = Offset(endX, 0f),
                                end = Offset(endX, h),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                // Subtitles & Text overlay style mapper
                if (uiState.overlayText.isNotEmpty()) {
                    val fontColor = when (uiState.overlayStyle) {
                        "CINEMATIC_YELLOW" -> Color(0xFFFFD54F)
                        "ALERT_RED" -> Color(0xFFFF5252)
                        else -> Color.White
                    }
                    val bgBrush = when (uiState.overlayStyle) {
                        "NEON_CYAN_BG" -> Color(0xFF00E5FF).copy(alpha = 0.35f)
                        "CINEMATIC_YELLOW" -> Color.Black.copy(alpha = 0.6f)
                        else -> Color.Transparent
                    }
                    val paddingValues = if (bgBrush != Color.Transparent) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(0.dp)
                    val shape = RoundedCornerShape(6.dp)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = (200.dp * uiState.textPositionPercent).coerceIn(20.dp, 160.dp))
                            .clip(shape)
                            .background(bgBrush)
                            .padding(paddingValues)
                    ) {
                        Text(
                            text = uiState.overlayText,
                            color = fontColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                if (uiState.isAiObjectRemovalEnabled || uiState.isAiUpscaleEnabled) {
                    NeuralMapOverlay(uiState = uiState)
                }
                MonitorOverlays(uiState = uiState)
                if (uiState.isSmartSilenceRemovalEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "تصفية الصمت AI نشط",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState.selectedFilter != CinematicFilter.NONE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(PremiereBlue.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "تأثير: ${uiState.selectedFilter.arabicName}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Overlay active aspect ratio display indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(SurfaceSlate.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "الأبعاد: ${uiState.aspectFormat}",
                        color = NeonCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // DYNAMIC RGB SIGNAL SCOPES (LUMETRI WAVEFORM SIMULATOR)
            if (cinemaMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0D14))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Histogram(uiState = uiState)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لوحة تحليل الإشارة في الوقت الفعلي (Lumetri Waveform)",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "R", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(text = "G", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(text = "B", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .background(Color(0xFF04060A), RoundedCornerShape(4.dp))
                            .border(BorderStroke(0.5.dp, SurfaceLight), RoundedCornerShape(4.dp))
                    ) {
                        // Animated Oscilloscope Scopes
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            // Draw horizontal reference lines
                            drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(0f, h * 0.1f), end = Offset(w, h * 0.1f))
                            drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(0f, h * 0.5f), end = Offset(w, h * 0.5f))
                            drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(0f, h * 0.9f), end = Offset(w, h * 0.9f))

                            val step = 4f

                            // Generate pseudo-random signal jitter based on playhead and animation frame
                            for (x in 0 until w.toInt() step step.toInt()) {
                                val fx = x.toFloat()
                                val waveFactor = Math.sin((fx * 0.05f + waveOffset * 0.2f).toDouble()).toFloat()
                                val noiseFactor = Math.cos((fx * 0.3f - waveOffset * 0.5f).toDouble()).toFloat() * 6f

                                val activeFactor = if (uiState.isPlaying) 1.5f else 0.1f
                                val yRed = midY - 4f + (waveFactor * 8f + noiseFactor) * activeFactor - (uiState.brightness * 15f)
                                val yGreen = midY + (waveFactor * 6f + noiseFactor * 0.8f) * activeFactor + (uiState.contrast * 5f) - 5f
                                val yBlue = midY + 4f + (waveFactor * 10f + noiseFactor * 1.2f) * activeFactor - (uiState.temperature * 12f)

                                drawCircle(color = Color.Red.copy(alpha = 0.4f), radius = 1.dp.toPx(), center = Offset(fx, yRed.coerceIn(0f, h)))
                                drawCircle(color = Color.Green.copy(alpha = 0.4f), radius = 1.dp.toPx(), center = Offset(fx, yGreen.coerceIn(0f, h)))
                                drawCircle(color = NeonCyan.copy(alpha = 0.4f), radius = 1.dp.toPx(), center = Offset(fx, yBlue.coerceIn(0f, h)))
                            }
                        }
                    }
                }
            }

            // Monitor Playback & Scrubber Controls Toolbar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time scrubber slider
                Slider(
                    value = progress,
                    onValueChange = { ratio ->
                        onSeek((ratio * duration).toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .testTag("monitor_scrubber"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = SurfaceLight
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Centered digital-style Time Display: "00:24 / 01:48"
                Text(
                    text = "${formatTime(uiState.playheadMs)} / ${formatTime(duration)}",
                    color = NeonCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Playback Buttons Group styled like the terminal screenshot
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. REWIND SQUARE BUTTON
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(BorderStroke(2.dp, NeonCyan), RoundedCornerShape(8.dp))
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = NeonCyan,
                                spotColor = NeonCyan
                            )
                            .background(Color.Black)
                            .clickable { onSeek((uiState.playheadMs - 5000L).coerceAtLeast(0L)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "تراجع ٥ ثواني",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 2. PLAY / PAUSE SQUARE BUTTON
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .border(BorderStroke(2.dp, NeonCyan), RoundedCornerShape(8.dp))
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = NeonCyan,
                                spotColor = NeonCyan
                            )
                            .background(Color.Black)
                            .clickable { onTogglePlay() }
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 3. FAST FORWARD SQUARE BUTTON
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(BorderStroke(2.dp, NeonCyan), RoundedCornerShape(8.dp))
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = NeonCyan,
                                spotColor = NeonCyan
                            )
                            .background(Color.Black)
                            .clickable { onSeek((uiState.playheadMs + 5000L).coerceAtMost(duration)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "تقديم ٥ ثواني",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}



/**
 * Handles picking media and extracting metadata offline using MediaMetadataRetriever
 */
@Composable
fun PreviewMonitor(
    uiState: EditorUiState,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val duration = uiState.currentMedia?.durationMs ?: 120000L
    val progress = if (duration > 0) uiState.playheadMs.toFloat() / duration else 0f

    // Animated canvas vector design representing dynamic frames that grade colors in real-time
    val infiniteTransition = rememberInfiniteTransition(label = "monitor_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    // Build the dynamic ColorMatrix to show live color grading in the monitor!
    val filterMatrix = uiState.selectedFilter.matrix
    val liveColorMatrix = remember(filterMatrix, uiState.brightness, uiState.contrast, uiState.saturation, uiState.temperature, uiState.isAiColorizationEnabled, uiState.aiColorizationIntensity, uiState.aiColorizationVibrancy) {
        val result = if (uiState.isAiColorizationEnabled) {
            // Convert to base grayscale representation of B&W historic photo/video
            val bw = ColorMatrix().apply { setToSaturation(0f) }
            val colorize = ColorMatrix().apply {
                // Infuse beautiful neural color tones based on sliders
                val intense = uiState.aiColorizationIntensity
                val vibe = uiState.aiColorizationVibrancy
                set(0, 0, 1.15f * intense)
                set(1, 1, 1.0f * vibe)
                set(2, 2, 0.85f * intense)
                set(0, 4, 25f * intense)
                set(1, 4, 10f * vibe)
                set(2, 4, -5f * intense)
            }
            bw.apply { timesAssign(colorize) }
        } else {
            ColorMatrix(filterMatrix)
        }

        // Custom brightness adjustment (row 0,1,2 offset indices 4, 9, 14 in raw float arrays, but here we can chain matrices)
        val brightnessMatrix = ColorMatrix().apply {
            setToScale(1f, 1f, 1f, 1f)
            // Apply brightness bias
            set(0, 4, uiState.brightness * 255f)
            set(1, 4, uiState.brightness * 255f)
            set(2, 4, uiState.brightness * 255f)
        }

        // Custom contrast and saturation adjustments
        val saturationMatrix = ColorMatrix().apply {
            setToSaturation(uiState.saturation)
        }

        val contrastMatrix = ColorMatrix().apply {
            val scale = uiState.contrast
            val translate = (-.5f * scale + .5f) * 255f
            set(0, 0, scale)
            set(1, 1, scale)
            set(2, 2, scale)
            set(0, 4, translate)
            set(1, 4, translate)
            set(2, 4, translate)
        }

        // Temperature matrix bias
        val tempMatrix = ColorMatrix().apply {
            set(0, 4, uiState.temperature * 50f) // Warm (Red bias)
            set(2, 4, -uiState.temperature * 50f) // Cool (Blue bias)
        }

        result.apply {
            timesAssign(brightnessMatrix)
            timesAssign(contrastMatrix)
            timesAssign(saturationMatrix)
            timesAssign(tempMatrix)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SurfaceLight),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            // Monitor Header info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (uiState.isPlaying) Color.Red else TextSecondary, CircleShape)
                    )
                    Text(
                        text = if (uiState.isPlaying) "بث مباشر (معاينة)" else "شاشة العرض (متوقف)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (uiState.isAiUpscaleEnabled) "دقة عصبية فائقة: 8K AI (مفعّل)" else "دقة العرض: 4K UHD 60fps",
                    color = if (uiState.isAiUpscaleEnabled) NeonCyan else Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (uiState.isAiUpscaleEnabled) FontWeight.Bold else FontWeight.Normal
                )
            }

            // The Viewport showing our custom live graded vector graphics representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF07090C))
                    .testTag("preview_monitor"),
                contentAlignment = Alignment.Center
            ) {
                // Vector landscape illustration styled dynamically by the ColorMatrix filter
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Background Gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF131A26), Color(0xFF080C14))
                        ),
                        size = size
                    )

                    // Clip to prevent drawing out of bounds
                    clipRect {
                        // Drawing moving layered mountain curves representing original high-fidelity video concepts
                        // We use direct drawing operations so the ColorFilter is applied beautifully!

                        // Sun
                        drawCircle(
                            color = Color(0xFFFFB300),
                            radius = 45.dp.toPx(),
                            center = Offset(w * 0.7f, h * 0.4f),
                            colorFilter = ColorFilter.colorMatrix(liveColorMatrix)
                        )

                        // Far Mountains (animated)
                        val farPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, h * 0.65f)
                            quadraticTo(w * 0.25f, h * 0.55f + Math.sin(Math.toRadians(waveOffset.toDouble())).toFloat() * 10f, w * 0.5f, h * 0.7f)
                            quadraticTo(w * 0.75f, h * 0.8f + Math.cos(Math.toRadians(waveOffset.toDouble())).toFloat() * 10f, w, h * 0.6f)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(
                            path = farPath,
                            color = Color(0xFF2A3B50),
                            colorFilter = ColorFilter.colorMatrix(liveColorMatrix)
                        )

                        // Near Hills (darker, high-contrast)
                        val nearPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, h * 0.8f)
                            quadraticTo(w * 0.35f, h * 0.72f, w * 0.65f, h * 0.85f)
                            quadraticTo(w * 0.85f, h * 0.9f, w, h * 0.75f)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(
                            path = nearPath,
                            color = Color(0xFF141F2E),
                            colorFilter = ColorFilter.colorMatrix(liveColorMatrix)
                        )

                        // Draggable playback sweep scanner visual
                        val playheadX = w * progress
                        drawLine(
                            color = PremiereOrange.copy(alpha = 0.8f),
                            start = Offset(playheadX, 0f),
                            end = Offset(playheadX, h),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawCircle(
                            color = PremiereOrange,
                            radius = 6.dp.toPx(),
                            center = Offset(playheadX, 10.dp.toPx())
                        )
                    }
                }

                // AI Silence indicators / Cut hints overlay (if enabled)
                if (uiState.isSmartSilenceRemovalEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "تصفية الصمت AI نشط",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Overlay active preset filter title tag
                if (uiState.selectedFilter != CinematicFilter.NONE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(PremiereBlue.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "تأثير: ${uiState.selectedFilter.arabicName}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Monitor Playback & Scrubber Controls Toolbar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time scrubber slider
                Slider(
                    value = progress,
                    onValueChange = { ratio ->
                        onSeek((ratio * duration).toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .testTag("monitor_scrubber"),
                    colors = SliderDefaults.colors(
                        thumbColor = PremiereOrange,
                        activeTrackColor = PremiereOrange.copy(alpha = 0.7f),
                        inactiveTrackColor = SurfaceLight
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current Position Display
                    Text(
                        text = formatTime(uiState.playheadMs),
                        color = PremiereOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Playback Buttons Group
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSeek((uiState.playheadMs - 5000L).coerceAtLeast(0L)) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay5,
                                contentDescription = "تراجع ٥ ثواني",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // PLAY / PAUSE button with dynamic icon
                        FloatingActionButton(
                            onClick = onTogglePlay,
                            containerColor = PremiereBlue,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSeek((uiState.playheadMs + 5000L).coerceAtMost(duration)) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward5,
                                contentDescription = "تقديم ٥ ثواني",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Total Duration Display
                    Text(
                        text = formatTime(duration),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * 3. Timeline Tracks Stack (Inspired by Adobe Premiere tracks)
 */
@Composable
fun Histogram(
    uiState: EditorUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0D14))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "توزيع مستويات الإضاءة (Histogram)",
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(Color(0xFF04060A), RoundedCornerShape(4.dp))
                .border(BorderStroke(0.5.dp, SurfaceLight), RoundedCornerShape(4.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw dynamic histogram curves based on brightness/contrast
                val brightnessFactor = uiState.brightness * 0.5f
                val contrastFactor = uiState.contrast

                fun drawChannel(color: Color, offset: Float) {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, h)

                    val points = 50
                    for (i in 0..points) {
                        val x = w * (i.toFloat() / points)
                        // Bell curve shifted by brightness and stretched by contrast
                        val normalizedX = (i.toFloat() / points) - 0.5f - brightnessFactor + offset
                        val y = h - (Math.exp(-(normalizedX * normalizedX) * 10f * contrastFactor).toFloat() * h * 0.8f)
                        path.lineTo(x, y.coerceIn(0f, h))
                    }
                    path.lineTo(w, h)
                    path.close()
                    drawPath(path = path, color = color.copy(alpha = 0.3f))
                }

                drawChannel(Color.Red, -0.05f)
                drawChannel(Color.Green, 0f)
                drawChannel(NeonCyan, 0.05f)
            }
        }
    }
}
