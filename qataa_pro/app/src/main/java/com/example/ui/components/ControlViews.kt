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
fun TitlesSubtitlesControlView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Aspect Ratio Selector
        Text(
            text = "أبعاد ونسبة تقسيم شاشة الفيديو (Aspect Ratio):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "16:9" to "أفلام (16:9) 🎬",
                "9:16" to "ريلز (9:16) 📱",
                "1:1" to "مربع (1:1) 🟩",
                "4:3" to "كلاسيكي (4:3) 📺"
            ).forEach { (format, label) ->
                val isSelected = uiState.aspectFormat == format
                Button(
                    onClick = { viewModel.updateAspectFormat(format) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PremiereBlue else SurfaceLight
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Divider(color = SurfaceLight)

        // Title text input
        Text(
            text = "تراكب النصوص والترجمة التلقائية (Live Text Overlay):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        OutlinedTextField(
            value = uiState.overlayText,
            onValueChange = { viewModel.updateOverlayText(it) },
            placeholder = { Text("اكتب نصاً ليعرض فوق الفيديو...", color = TextSecondary, fontSize = 11.sp) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = SurfaceLight,
                cursorColor = NeonCyan
            ),
            modifier = Modifier.fillMaxWidth().testTag("overlay_text_input")
        )

        // Preset templates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "قطع سينمائي مذهل 🎬" to "سينمائي",
                "تابعونا للمزيد! 🔔" to "دعوة للمتابعة",
                "بث مباشر بجودة 4K 🔥" to "بث مباشر"
            ).forEach { (presetText, label) ->
                SuggestionChip(
                    onClick = { viewModel.updateOverlayText(presetText) },
                    label = { Text(text = label, fontSize = 9.sp, color = NeonCyan) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SurfaceLight.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, SurfaceLight)
                )
            }
        }

        Divider(color = SurfaceLight)

        // Custom Overlay style selector
        Text(
            text = "نمط ولون الخط التوضيحي:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "CLASSIC_WHITE" to "أبيض كلاسيكي",
                "CINEMATIC_YELLOW" to "أصفر سينمائي",
                "NEON_CYAN_BG" to "تأثير نيون مضيء",
                "ALERT_RED" to "تنبيه أحمر"
            ).forEach { (style, label) ->
                val isSelected = uiState.overlayStyle == style
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PremiereBlue.copy(alpha = 0.15f) else SurfaceLight
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.updateOverlayStyle(style) }
                        .height(34.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = label, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Text Position slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "موضع النص الرأسي:", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(110.dp))
            Slider(
                value = uiState.textPositionPercent,
                onValueChange = { viewModel.updateTextPosition(it) },
                valueRange = 0.1f..0.9f,
                modifier = Modifier.weight(1f).testTag("text_position_slider"),
                colors = SliderDefaults.colors(thumbColor = NeonCyan)
            )
            Text(text = if (uiState.textPositionPercent < 0.4f) "أعلى" else if (uiState.textPositionPercent > 0.7f) "أسفل" else "منتصف", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
        }
    }
}

/**
 * Tab Content: 🎵 Soundtrack Mixer View
 */
@Composable
fun AudioMixerControlView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "إضافة مسار موسيقي خلفي (Soundtrack Mixer):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "NONE" to ("صوت الفيديو الأصلي فقط" to "بدون إضافة أي موسيقى خلفية للمشهد"),
                "CINEMATIC_EPIC" to ("موسيقى سينمائية ملحمية 🎵" to "ألحان أوركسترالية حماسية هادئة للمقاطع الاحترافية"),
                "LOFI_CHILL" to ("أصوات لو فاي هادئة Lofi ☕" to "إيقاعات مريحة وهادئة مثالية للمدونات والشروحات"),
                "VLOG_HAPPY" to ("موسيقى فلوق مرحة ✨" to "ألحان إيجابية مفعمة بالنشاط والمرح لليوتيوب")
            ).forEach { (trackKey, data) ->
                val (title, desc) = data
                val isSelected = uiState.soundtrack == trackKey

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) PremiereBlue.copy(alpha = 0.15f) else SurfaceLight,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateSoundtrack(trackKey) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.updateSoundtrack(trackKey) },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = desc,
                            fontSize = 8.sp,
                            color = TextSecondary,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        if (uiState.soundtrack != "NONE") {
            Divider(color = SurfaceLight)

            // Volume adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "مستوى صوت الموسيقى:", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(110.dp))
                Slider(
                    value = uiState.soundtrackVolume,
                    onValueChange = { viewModel.updateSoundtrackVolume(it) },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f).testTag("soundtrack_volume_slider"),
                    colors = SliderDefaults.colors(thumbColor = NeonCyan)
                )
                Text(text = "${(uiState.soundtrackVolume * 100).toInt()}%", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }
        }

        Divider(color = SurfaceLight)

        // Original video audio controls (Mute / Unmute)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleOriginalAudioMute(!uiState.isOriginalAudioMuted) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "كتم الصوت الأصلي للمقطع (Mute Original Sound)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "كتم صوت الحديث أو البيئة الأصلي للتركيز بالكامل على الموسيقى المضافة ومكساج الصوت الاحترافي.",
                    fontSize = 8.sp,
                    color = TextSecondary,
                    lineHeight = 11.sp
                )
            }
            Switch(
                checked = uiState.isOriginalAudioMuted,
                onCheckedChange = { viewModel.toggleOriginalAudioMute(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = PremiereBlue
                ),
                modifier = Modifier.testTag("mute_original_switch")
            )
        }
    }
}

/**
 * Tab Content: ✂️ Cutting & Splitting View
 */
@Composable
fun SplitControlView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "تحديد أسلوب ونظام التقسيم",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Split Mode Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Manual/Current Range cut mode
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.splitMode == SplitMode.BY_PARTS) SurfaceLight else Color.Transparent
                ),
                border = BorderStroke(
                    1.dp,
                    if (uiState.splitMode == SplitMode.BY_PARTS) NeonCyan else SurfaceLight
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updateSplitMode(SplitMode.BY_PARTS) }
                    .testTag("split_by_parts_card"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = if (uiState.splitMode == SplitMode.BY_PARTS) NeonCyan else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تقسيم لقطع متساوية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "تقطيع لمقاطع متساوية العدد",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Fixed duration cut mode (WhatsApp / Stories)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.splitMode == SplitMode.BY_DURATION) SurfaceLight else Color.Transparent
                ),
                border = BorderStroke(
                    1.dp,
                    if (uiState.splitMode == SplitMode.BY_DURATION) NeonCyan else SurfaceLight
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updateSplitMode(SplitMode.BY_DURATION) }
                    .testTag("split_by_duration_card"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = if (uiState.splitMode == SplitMode.BY_DURATION) NeonCyan else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تقسيم بمدد زمنية محددة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "مثالي لقصص انستغرام وواتساب",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Divider(color = SurfaceLight)

        // Dynamic parameter adjustment based on selected mode
        if (uiState.splitMode == SplitMode.BY_PARTS) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عدد القطع المطلوب تقسيم المقطع إليها:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${uiState.partsCount} أجزاء متساوية",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = uiState.partsCount.toFloat(),
                    onValueChange = { viewModel.updatePartsCount(it.toInt()) },
                    valueRange = 2f..24f,
                    steps = 21,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("parts_count_slider")
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المدة الزمنية لكل قطعة:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${uiState.segmentDurationSec} ثانية لكل جزء",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Presets buttons for duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        15 to "حالة واتساب (١٥ث)",
                        30 to "قصة انستغرام (٣٠ث)",
                        60 to "تيك توك ريلز (٦٠ث)"
                    ).forEach { (seconds, label) ->
                        Button(
                            onClick = { viewModel.updateSegmentDuration(seconds) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.segmentDurationSec == seconds) PremiereBlue else SurfaceLight
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Slider(
                    value = uiState.segmentDurationSec.toFloat(),
                    onValueChange = { viewModel.updateSegmentDuration(it.toInt()) },
                    valueRange = 5f..120f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("duration_sec_slider")
                )
            }
        }

        // Live Grid Visualizer representing cutting boundaries
        Text(
            text = "المخطط الهيكلي للأجزاء المنتجة:",
            fontSize = 10.sp,
            color = TextSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color.Black, RoundedCornerShape(6.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val totalSegments = if (uiState.splitMode == SplitMode.BY_PARTS) {
                uiState.partsCount
            } else {
                val mediaLenSec = (uiState.selectionEndMs - uiState.selectionStartMs) / 1000
                (mediaLenSec / uiState.segmentDurationSec).toInt().coerceAtLeast(1)
            }

            for (i in 0 until totalSegments.coerceAtMost(12)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TrackVideoColor.copy(alpha = 0.5f),
                                    TrackVideoColor
                                )
                            ),
                            RoundedCornerShape(3.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${i + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (totalSegments > 12) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .fillMaxHeight()
                        .background(SurfaceLight, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+${totalSegments - 12}", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. AI Object Removal (AI Eraser)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleAiObjectRemoval(!uiState.isAiObjectRemovalEnabled) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مساعد المسح الذكي (AI Object Removal)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "إزالة العناصر غير المرغوب فيها من الفيديو أو الصور باستخدام تقنية الترميم العصبي (Inpainting) محلياً.",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
            Switch(
                checked = uiState.isAiObjectRemovalEnabled,
                onCheckedChange = { viewModel.toggleAiObjectRemoval(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = PremiereBlue
                )
            )
        }

        // 7. Neural Voice Isolation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleAiVoiceIsolation(!uiState.isAiVoiceIsolationEnabled) }
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "عزل الصوت البشري (Neural Voice Isolation)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "فصل صوت المتحدث عن الضجيج الخلفي والموسيقى باستخدام شبكة Deep-Separation المحلية.",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp
                    )
                }
                Switch(
                    checked = uiState.isAiVoiceIsolationEnabled,
                    onCheckedChange = { viewModel.toggleAiVoiceIsolation(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SuccessGreen,
                        checkedTrackColor = PremiereBlue
                    )
                )
            }

            if (uiState.isAiVoiceIsolationEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "قوة العزل:", fontSize = 10.sp, color = Color.White, modifier = Modifier.width(60.dp))
                    Slider(
                        value = uiState.aiVoiceIsolationIntensity,
                        onValueChange = { viewModel.updateAiVoiceIsolationIntensity(it) },
                        modifier = Modifier.weight(1f).height(18.dp),
                        colors = SliderDefaults.colors(thumbColor = SuccessGreen)
                    )
                }
            }
        }
    }
}

/**
 * Tab Content: 🎨 Color Grading & Cinematic Filters View
 */
@Composable
fun ColorGradingControlView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الفلاتر السينمائية الاحترافية (بضغطة واحدة)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "إعادة ضبط المستويات ⟲",
                fontSize = 11.sp,
                color = PremiereOrange,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { viewModel.resetColorGrading() }
            )
        }

        // Horizontal list of filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CinematicFilter.values()) { filter ->
                val isSelected = uiState.selectedFilter == filter
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .clickable { viewModel.updateFilter(filter) }
                        .testTag("filter_card_${filter.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PremiereBlue.copy(alpha = 0.2f) else SurfaceLight
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) NeonCyan else Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Tiny representation box with gradient representing color matrix
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = when (filter) {
                                            CinematicFilter.NONE -> listOf(Color.Gray, Color.DarkGray)
                                            CinematicFilter.WARM_CINEMA -> listOf(Color(0xFFE29B00), Color(0xFF7C5601))
                                            CinematicFilter.TEAL_ORANGE -> listOf(Color(0xFF00BFA5), Color(0xFFFF6D00))
                                            CinematicFilter.NOIR -> listOf(Color.White, Color.Black)
                                            CinematicFilter.CYBERPUNK -> listOf(Color(0xFFD500F9), Color(0xFF00E5FF))
                                            CinematicFilter.COLD_THRILLER -> listOf(Color(0xFF0091EA), Color(0xFF002F6C))
                                        }
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = filter.arabicName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Divider(color = SurfaceLight)

        // Micro Adjustments
        Text(
            text = "المستويات والألوان التفصيلية (تحكم دقيق)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        // Brightness Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "الإضاءة:", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(64.dp))
            Slider(
                value = uiState.brightness,
                onValueChange = { viewModel.updateColorGrading(it, uiState.contrast, uiState.saturation, uiState.temperature) },
                valueRange = -0.4f..0.4f,
                modifier = Modifier.weight(1f).testTag("brightness_slider"),
                colors = SliderDefaults.colors(thumbColor = NeonCyan)
            )
            Text(text = String.format("%+.1f", uiState.brightness * 10), fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
        }

        // Contrast Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "التباين:", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(64.dp))
            Slider(
                value = uiState.contrast,
                onValueChange = { viewModel.updateColorGrading(uiState.brightness, it, uiState.saturation, uiState.temperature) },
                valueRange = 0.5f..1.5f,
                modifier = Modifier.weight(1f).testTag("contrast_slider"),
                colors = SliderDefaults.colors(thumbColor = NeonCyan)
            )
            Text(text = String.format("%.1fx", uiState.contrast), fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
        }

        // Saturation Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "التشبع:", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(64.dp))
            Slider(
                value = uiState.saturation,
                onValueChange = { viewModel.updateColorGrading(uiState.brightness, uiState.contrast, it, uiState.temperature) },
                valueRange = 0.0f..2.0f,
                modifier = Modifier.weight(1f).testTag("saturation_slider"),
                colors = SliderDefaults.colors(thumbColor = NeonCyan)
            )
            Text(text = String.format("%.1fx", uiState.saturation), fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
        }

        // Temperature Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "الحرارة:", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(64.dp))
            Slider(
                value = uiState.temperature,
                onValueChange = { viewModel.updateColorGrading(uiState.brightness, uiState.contrast, uiState.saturation, it) },
                valueRange = -0.5f..0.5f,
                modifier = Modifier.weight(1f).testTag("temp_slider"),
                colors = SliderDefaults.colors(thumbColor = NeonCyan)
            )
            Text(text = if (uiState.temperature > 0) "دافئ" else if (uiState.temperature < 0) "بارد" else "طبيعي", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
        }
    }
}

/**
 * Tab Content: 🤖 Local AI Tools View
 */
@Composable
fun LocalAiControlView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(PremiereOrange.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚡", color = PremiereOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "مساعد المونتاج الذكي محلياً (بدون إنترنت)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "تم بناء نظام ذكاء اصطناعي محلي يعمل بالكامل على نواة الأندرويد لتسريع معالجة ملفات 4K الفائقة وتقليل استخدام موارد المعالج والبطارية لتفادي ارتفاع حرارة الهاتف أثناء المونتاج.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )

        Divider(color = SurfaceLight)

        // 1. Silence Remover AI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleSmartSilenceRemoval(!uiState.isSmartSilenceRemovalEnabled) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تقطيع الصمت التلقائي (Silence Remover)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "تحليل الطيف الصوتي والتقطيع التلقائي للفراغات والصمت لتسريع إيقاع الفيديو والحديث.",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
            Switch(
                checked = uiState.isSmartSilenceRemovalEnabled,
                onCheckedChange = { viewModel.toggleSmartSilenceRemoval(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = PremiereBlue
                ),
                modifier = Modifier.testTag("silence_switch")
            )
        }

        // 2. Low CPU Export (Safety Cooling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleLowCpuExport(!uiState.isLowCpuExportEnabled) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "نظام التبريد الآمن (Cooling Mode)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(text = "موصى به لـ 4K", color = SuccessGreen, fontSize = 7.sp, fontWeight = FontWeight.Black)
                    }
                }
                Text(
                    text = "تنظيم كتابة الإطارات لمنع ارتفاع حرارة معالج الهاتف بنسبة ٤٥٪ وتمديد عمر البطارية أثناء عمليات التصدير الطويلة.",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
            Switch(
                checked = uiState.isLowCpuExportEnabled,
                onCheckedChange = { viewModel.toggleLowCpuExport(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = PremiereBlue
                ),
                modifier = Modifier.testTag("low_cpu_switch")
            )
        }

        // 3. AI Color Enhancer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleAiEnhance(!uiState.isAiEnhanceEnabled) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ترميم وتفتيح الإطارات بالذكاء الاصطناعي",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "تحسين ألوان وظلال إطارات الفيديو ذات الإضاءة الضعيفة لزيادة التباين دون تبهيت الجوانب الطبيعية للمشهد.",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
            Switch(
                checked = uiState.isAiEnhanceEnabled,
                onCheckedChange = { viewModel.toggleAiEnhance(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = PremiereBlue
                ),
                modifier = Modifier.testTag("ai_enhance_switch")
            )
        }

        // 4. AI Super Resolution / Upscaling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleAiUpscale(!uiState.isAiUpscaleEnabled) }
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "رفع الدقة والوضوح (Super Resolution)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "محلي 100%", color = NeonCyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "مضاعفة جودة الصور والفيديو ومعالجة البيكسلات الضبابية باستخدام شبكات عصبية مدمجة تضمن بقاء التفاصيل حادة دون استهلاك إنترنت.",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp
                    )
                }
                Switch(
                    checked = uiState.isAiUpscaleEnabled,
                    onCheckedChange = { viewModel.toggleAiUpscale(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = PremiereBlue
                    ),
                    modifier = Modifier.testTag("ai_upscale_switch")
                )
            }

            if (uiState.isAiUpscaleEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = DarkSlateBackground, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "اختر طراز الذكاء الاصطناعي المحلي النشط:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                val upscaleModels = listOf(
                    Triple("Real-ESRGAN Mobile 2x (Offline)", "١٢٠ ميجابايت • توازن فائق وثبات تفاصيل", "RAM: 1.4 GB"),
                    Triple("Super-SwinIR x4 (Offline)", "٢٥٠ ميجابايت • تفاصيل قصوى وإطارات ممتازة", "RAM: 2.1 GB"),
                    Triple("Mobile-SR Light (Offline)", "٤٥ ميجابايت • خفيف جداً ومثالي للهواتف القديمة", "RAM: 0.6 GB")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    upscaleModels.forEach { (modelName, details, ram) ->
                        val isSelected = uiState.aiUpscaleModel == modelName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) NeonCyan.copy(alpha = 0.08f) else DarkSlateBackground,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.updateAiUpscaleModel(modelName) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateAiUpscaleModel(modelName) },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = modelName.replace(" (Offline)", ""),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonCyan else Color.White
                                )
                                Text(
                                    text = details,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(SurfaceLight, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = ram, color = TextSecondary, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5. AI Colorization (تلوين الصور والفيديو)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleAiColorization(!uiState.isAiColorizationEnabled) }
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "تلوين أبيض وأسود ذكي (AI Colorization)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(NeonPink.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "بدون إنترنت", color = NeonPink, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "إعادة الحياة والتلوين الواقعي للقططات أو الصور القديمة ذات اللونين الأبيض والأسود بالاعتماد على شبكات التنبؤ اللوني محلياً.",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp
                    )
                }
                Switch(
                    checked = uiState.isAiColorizationEnabled,
                    onCheckedChange = { viewModel.toggleAiColorization(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonPink,
                        checkedTrackColor = PremiereBlue
                    ),
                    modifier = Modifier.testTag("ai_colorization_switch")
                )
            }

            if (uiState.isAiColorizationEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = DarkSlateBackground, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "اختر طراز التلوين الشبكي المحلي النشط:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                val colorModels = listOf(
                    Pair("DeOldify Lite (Offline)", "طراز عالي التوازن للأفلام التاريخية والوثائقية القديمة"),
                    Pair("Siggraph17 Mobile (Offline)", "طراز فائق الخفة ومثالي لصور الوجوه والأشخاص بدقة طبيعية"),
                    Pair("Chroma-Net v2 (Offline)", "تلوين فني مشبع وممتاز للمناظر الطبيعية واللقطات الخارجية")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    colorModels.forEach { (modelName, details) ->
                        val isSelected = uiState.aiColorizationModel == modelName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) NeonPink.copy(alpha = 0.08f) else DarkSlateBackground,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    BorderStroke(1.dp, if (isSelected) NeonPink else Color.Transparent),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.updateAiColorizationModel(modelName) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateAiColorizationModel(modelName) },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonPink)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = modelName.replace(" (Offline)", ""),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonPink else Color.White
                                )
                                Text(
                                    text = details,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "مستويات موازنة الألوان الشبكية للنموذج:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Intensity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "كثافة التلوين:", fontSize = 10.sp, color = Color.White, modifier = Modifier.width(75.dp))
                    Slider(
                        value = uiState.aiColorizationIntensity,
                        onValueChange = { viewModel.updateAiColorizationIntensity(it) },
                        valueRange = 0.0f..2.0f,
                        modifier = Modifier.weight(1f).height(18.dp),
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink.copy(alpha = 0.5f))
                    )
                    Text(text = String.format("%.1fx", uiState.aiColorizationIntensity), fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Vibrancy Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "حيوية الألوان:", fontSize = 10.sp, color = Color.White, modifier = Modifier.width(75.dp))
                    Slider(
                        value = uiState.aiColorizationVibrancy,
                        onValueChange = { viewModel.updateAiColorizationVibrancy(it) },
                        valueRange = 0.0f..2.0f,
                        modifier = Modifier.weight(1f).height(18.dp),
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink.copy(alpha = 0.5f))
                    )
                    Text(text = String.format("%.1fx", uiState.aiColorizationVibrancy), fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

/**
 * Tab Content: ⚡ Smart Compression & Export Parameters View
 */
@Composable
fun CompressionControlView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    val currentMedia = uiState.currentMedia
    val isImg = currentMedia?.isImage == true
    val isVid = currentMedia?.isVideo == true
    val isAud = currentMedia != null && !isImg && !isVid

    val origSizeMb = (currentMedia?.sizeBytes ?: 482344960L) / (1024.0 * 1024.0)

    // Calculate compression level ratio
    val compRatio = when (uiState.compressionLevel) {
        CompressionLevel.MAX -> 0.15
        CompressionLevel.BALANCED -> 0.45
        CompressionLevel.ULTRA_HQ -> 0.85
    }

    // Codec efficiency multiplier
    val codecBonus = when (uiState.targetCodec) {
        "AV1" -> 0.40  // AV1 is 60% more compressed than H.264
        "HEVC" -> 0.55 // HEVC is 45% more compressed than H.264
        else -> 1.0
    }

    // Resolution efficiency multiplier
    val resBonus = when (uiState.targetResolution) {
        "4K" -> 1.0
        "1080P" -> 0.35
        "720P" -> 0.18
        "480P" -> 0.08
        else -> 1.0 // Original
    }

    var finalSavingsMultiplier = compRatio * codecBonus
    if (isVid) {
        finalSavingsMultiplier *= resBonus
    }
    val targetMultiplier = finalSavingsMultiplier.coerceIn(0.05, 0.95)
    val expectedCompSizeMb = if (uiState.isCompressionEnabled) origSizeMb * targetMultiplier else origSizeMb

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Tab Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "محول الصيغ ومحرك الضغط فائق الدقة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Global compression switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (uiState.isCompressionEnabled) "الضغط مفعل" else "الضغط معطل",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Switch(
                    checked = uiState.isCompressionEnabled,
                    onCheckedChange = { viewModel.toggleCompression(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = PremiereBlue
                    ),
                    modifier = Modifier.testTag("compression_switch")
                )
            }
        }

        Text(
            text = "تعديل وضغط الملفات محلياً بنسبة تصل إلى 90% مع الحفاظ الكامل على الهوية البصرية وجودة الألوان والـ FPS وعمق الصوت، ودعم تحويل جميع صيغ الفيديو والصوت والصور.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )

        if (isVid) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, RoundedCornerShape(8.dp))
                    .border(
                        BorderStroke(1.dp, if (uiState.isCompressionEnabled && uiState.targetCodec == "HEVC") NeonCyan.copy(alpha = 0.5f) else Color.Transparent),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable {
                        val currentlyActive = uiState.isCompressionEnabled && uiState.targetCodec == "HEVC"
                        if (currentlyActive) {
                            viewModel.toggleCompression(false)
                        } else {
                            viewModel.toggleCompression(true)
                            viewModel.updateTargetCodec("HEVC")
                        }
                    }
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تفعيل ضغط H.265 (HEVC) فائق الجودة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = "محلي 100%", color = NeonCyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "تقليص مساحة التخزين بنسبة تصل إلى 90% مع الحفاظ المطلق على حدة التفاصيل، الألوان، الـ FPS وعمق الصوت باستخدام نواة الترميز المحلي المتطورة H.265 دون الحاجة للإنترنت.",
                            fontSize = 9.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                    Switch(
                        checked = uiState.isCompressionEnabled && uiState.targetCodec == "HEVC",
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                viewModel.toggleCompression(true)
                                viewModel.updateTargetCodec("HEVC")
                            } else {
                                viewModel.toggleCompression(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = PremiereBlue
                        ),
                        modifier = Modifier.testTag("hevc_compression_switch")
                    )
                }
            }
        }

        Divider(color = SurfaceLight)

        // --- SECTION 1: Local Format Conversion ---
        Text(
            text = "🔄 تحويل الصيغة والامتداد محلياً:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Target formats list depending on currently loaded media
        val availableFormats = when {
            isImg -> listOf("WEBP", "PNG", "JPG", "HEIC", "BMP", "GIF")
            isVid -> listOf("MP4", "MKV", "MOV", "AVI", "WEBM", "FLV", "MP3", "WAV", "AAC") // Includes audio extraction!
            else -> listOf("MP3", "WAV", "AAC", "FLAC", "OGG", "AMR")
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isVid) "اختر صيغة الإخراج المطلوبة (اختر صيغة صوتية لاستخراج الصوت من الفيديو):" else "اختر امتداد وصيغة الملف المستهدفة محلياً:",
                fontSize = 10.sp,
                color = TextSecondary
            )

            // Flowable grid of format chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableFormats.forEach { format ->
                    val isSelected = uiState.targetFormat == format
                    val isAudioExtraction = isVid && format in listOf("MP3", "WAV", "AAC")

                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) PremiereBlue.copy(alpha = 0.3f) else SurfaceLight,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateTargetFormat(format) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isAudioExtraction -> Icons.Default.MusicNote
                                    format in listOf("MP3", "WAV", "AAC", "FLAC", "OGG") -> Icons.Default.AudioFile
                                    format in listOf("PNG", "JPG", "WEBP", "BMP") -> Icons.Default.Image
                                    else -> Icons.Default.VideoLibrary
                                },
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = format + if (isAudioExtraction) " (استخراج صوت)" else "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonCyan else Color.White
                            )
                        }
                    }
                }
            }
        }

        Divider(color = SurfaceLight)

        // --- SECTION 2: Advanced Compression Codecs ---
        if (uiState.isCompressionEnabled) {
            if (isVid && uiState.targetFormat !in listOf("MP3", "WAV", "AAC")) {
                Text(
                    text = "⚙️ ترميز الضغط المتقدم (Codec):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val codecs = listOf(
                        Triple("HEVC", "ترميز H.265 / HEVC", "توفير 45% إضافي + دقة 4K"),
                        Triple("AV1", "ترميز AV1 الحديث", "أقصى ضغط 60% + جودة فائقة"),
                        Triple("H264", "ترميز H.264 / AVC", "التوافق الكلي والسريع")
                    )

                    codecs.forEach { (codecKey, title, desc) ->
                        val isSelected = uiState.targetCodec == codecKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) PremiereBlue.copy(alpha = 0.2f) else SurfaceLight,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.updateTargetCodec(codecKey) }
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonCyan else Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 8.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }

                Divider(color = SurfaceLight)
            }

            // --- SECTION 3: Compression Quality Level Presets ---
            Text(
                text = "📊 مستوى ونسبة الضغط المطلوبة:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompressionLevel.values().forEach { level ->
                    val isSelected = uiState.compressionLevel == level

                    // Show custom small size multipliers
                    val savingsDesc = when (level) {
                        CompressionLevel.MAX -> "تصغير فائق للمساحة حتى 85% - مثالي لمواقع التواصل الاجتماعي"
                        CompressionLevel.BALANCED -> "توفير 55% من المساحة - جودة سينمائية فائقة وصور حادة"
                        CompressionLevel.ULTRA_HQ -> "توفير 15% من المساحة - الحفاظ على أعلى بت-ريت وبيكسلات سينمائية"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) PremiereBlue.copy(alpha = 0.15f) else SurfaceLight,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateCompressionLevel(level) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.updateCompressionLevel(level) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = level.arabicName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = savingsDesc,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            Divider(color = SurfaceLight)

            // --- SECTION 4: Advanced Output Specifications ---
            Text(
                text = "⚙️ إعدادات الإخراج المتقدمة (اختياري):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Resolution Selector (if video and not audio extraction)
                if (isVid && uiState.targetFormat !in listOf("MP3", "WAV", "AAC")) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "تغيير دقة الفيديو (Resolution):", fontSize = 10.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val resolutions = listOf(
                                "ORIGINAL" to "الأصلية",
                                "4K" to "4K UHD",
                                "1080P" to "1080p FHD",
                                "720P" to "720p HD",
                                "480P" to "480p SD"
                            )
                            resolutions.forEach { (resKey, label) ->
                                val isResSelected = uiState.targetResolution == resKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isResSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceLight,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isResSelected) NeonCyan else Color.Transparent),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.updateTargetResolution(resKey) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isResSelected) NeonCyan else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Audio Bitrate Selector (for audios and video)
                if (isVid || isAud) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "معدل بت الصوت (Audio Bitrate):", fontSize = 10.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val bitrates = listOf(
                                "320" to "320 kbps (جودة الاستوديو)",
                                "192" to "192 kbps (جودة عالية متوازنة)",
                                "128" to "128 kbps (جودة مضغوطة قياسية)"
                            )
                            bitrates.forEach { (bitrateKey, label) ->
                                val isBitSelected = uiState.targetAudioBitrate == bitrateKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isBitSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceLight,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isBitSelected) NeonCyan else Color.Transparent),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.updateTargetAudioBitrate(bitrateKey) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBitSelected) NeonCyan else Color.White,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Frame Rate selector (for video)
                if (isVid && uiState.targetFormat !in listOf("MP3", "WAV", "AAC")) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "معدل الإطارات في الثانية (Frame Rate):", fontSize = 10.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val fpsOptions = listOf("60", "30", "24")
                            fpsOptions.forEach { fps ->
                                val isFpsSelected = uiState.targetFps == fps
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isFpsSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceLight,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isFpsSelected) NeonCyan else Color.Transparent),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.updateTargetFps(fps) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$fps FPS" + if (fps == "60") " (سلاسة فائقة)" else if (fps == "30") " (موفر)" else " (سينما)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFpsSelected) NeonCyan else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // High Quality Color consistency switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تثبيت توازن ألوان HDR والكونسبت السينمائي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "يمنع بهتان ألوان بكسلات الصور والفيديو الناتج عن عمليات الضغط المعتادة محلياً",
                            fontSize = 8.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.isHighQualityColorPreserved,
                        onCheckedChange = { viewModel.toggleHighQualityColorPreserved(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = PremiereBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- SECTION 5: Live Compression Comparison Chart ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "المقارنة الرياضية والمساحة التخزينية المتوقعة:",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحجم الأصلي:",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.width(90.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .background(SurfaceLight, RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(1f)
                                    .background(PremiereOrange, RoundedCornerShape(5.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = String.format("%.1f MB", origSizeMb),
                            color = PremiereOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بعد المعالجة والضغط:",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.width(90.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .background(SurfaceLight, RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((expectedCompSizeMb / origSizeMb).toFloat().coerceIn(0.01f, 1f))
                                    .background(SuccessGreen, RoundedCornerShape(5.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = String.format("%.1f MB", expectedCompSizeMb),
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    // Total Saved Space Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .background(SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val savedPercent = (100 - (targetMultiplier * 100).toInt()).coerceIn(1, 99)
                        Text(
                            text = "مساحة تخزين مـوفـرة: $savedPercent% أقل من الأصلي بفضل تقنية ${uiState.targetCodec}!",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Disabled state warning
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarningYellow.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, WarningYellow.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "الضغط معطل حالياً. سيتم تصدير وتحويل صيغة الملف محلياً دون ضغط وتقليص للمساحة التخزينية.",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * 5. Fixed Bottom Export Action Bar Component
 */
        // 6. AI Object Removal (AI Eraser)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleAiObjectRemoval(!uiState.isAiObjectRemovalEnabled) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مساعد المسح الذكي (AI Object Removal)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "إزالة العناصر غير المرغوب فيها من الفيديو أو الصور باستخدام تقنية الترميم العصبي (Inpainting) محلياً.",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
            Switch(
                checked = uiState.isAiObjectRemovalEnabled,
                onCheckedChange = { viewModel.toggleAiObjectRemoval(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = PremiereBlue
                )
            )
        }

        // 7. Neural Voice Isolation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleAiVoiceIsolation(!uiState.isAiVoiceIsolationEnabled) }
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "عزل الصوت البشري (Neural Voice Isolation)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "فصل صوت المتحدث عن الضجيج الخلفي والموسيقى باستخدام شبكة Deep-Separation المحلية.",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp
                    )
                }
                Switch(
                    checked = uiState.isAiVoiceIsolationEnabled,
                    onCheckedChange = { viewModel.toggleAiVoiceIsolation(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SuccessGreen,
                        checkedTrackColor = PremiereBlue
                    )
                )
            }

            if (uiState.isAiVoiceIsolationEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "قوة العزل:", fontSize = 10.sp, color = Color.White, modifier = Modifier.width(60.dp))
                    Slider(
                        value = uiState.aiVoiceIsolationIntensity,
                        onValueChange = { viewModel.updateAiVoiceIsolationIntensity(it) },
                        modifier = Modifier.weight(1f).height(18.dp),
                        colors = SliderDefaults.colors(thumbColor = SuccessGreen)
                    )
                }
            }
        }
