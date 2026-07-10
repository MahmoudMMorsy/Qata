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
fun ExportProgressDialog(
    uiState: EditorUiState,
    onDismiss: () -> Unit,
    onStartExport: () -> Unit,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit
) {
    val progress = uiState.progress

    Dialog(
        onDismissRequest = { if (!uiState.isProcessing) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !uiState.isProcessing,
            dismissOnClickOutside = !uiState.isProcessing,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isProcessing) "جاري المعالجة والتصدير..." else if (uiState.exportFinished) "اكتمل التصدير بنجاح! 🎉" else "تأكيد تصدير وقص الملف",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (!uiState.isProcessing) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                            }
                        }
                    }

                    Divider(color = SurfaceLight)

                    // Mode 1: Confirmation page before starting
                    if (!uiState.isProcessing && !uiState.exportFinished) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "سيقوم البرنامج بتقطيع وحفظ الملف محلياً بجودة فائقة السرعة اعتماداً على معالجات هاتف الأندرويد مباشرة، وبدون استهلاك للإنترنت.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center
                            )

                            // Quick metrics list
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "صيغة المونتاج:", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = "MP4 (MPEG-4 Container)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "الدقة المستهدفة:", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = uiState.currentMedia?.resolutionString ?: "", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "الفلتر السينمائي:", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = uiState.selectedFilter.arabicName, fontSize = 11.sp, color = PremiereOrange, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "أسلوب التقطيع:", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = if (uiState.splitMode == SplitMode.BY_PARTS) "تقسيم لـ ${uiState.partsCount} قطع" else "تقسيم كل ${uiState.segmentDurationSec} ثانية",
                                        fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "مستوى ضغط المساحة:", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = if (uiState.isCompressionEnabled) uiState.compressionLevel.arabicName else "بدون ضغط الحجم",
                                        fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Custom Target Export Folder Selector
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "مجلد الحفظ والتصدير المستهدف:",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val folderLabel = uiState.customExportFolderName ?: "الاستوديو الافتراضي (Movies/Qataa)"
                                    val folderIcon = if (uiState.customExportFolderUri != null) Icons.Default.FolderSpecial else Icons.Default.Folder

                                    Icon(
                                        imageVector = folderIcon,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Text(
                                        text = folderLabel,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Button(
                                        onClick = onChooseFolder,
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(text = "تغيير", fontSize = 10.sp, color = NeonCyan)
                                    }

                                    if (uiState.customExportFolderUri != null) {
                                        IconButton(
                                            onClick = onClearFolder,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "مسح",
                                                tint = ErrorRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = onStartExport,
                                colors = ButtonDefaults.buttonColors(containerColor = PremiereBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_processing_now_button")
                            ) {
                                Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "تأكيد وبدء المعالجة والقص الفوري", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Mode 2: Loading State
                    if (uiState.isProcessing && progress != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Circular custom animated loading graphic
                            Box(
                                modifier = Modifier.size(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = progress.percentage,
                                    strokeWidth = 6.dp,
                                    color = NeonCyan,
                                    trackColor = SurfaceLight,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(progress.percentage * 100).toInt()}%",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "سرعة فائقة",
                                        color = SuccessGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Active Step Description text
                            Text(
                                text = progress.currentStep,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // Traditional Horizontal Progress Bar (Requested)
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = progress.percentage,
                                    color = NeonCyan,
                                    trackColor = SurfaceLight,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .testTag("horizontal_progress_bar")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "نسبة الإنجاز: ${(progress.percentage * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "المعالج: محلي 100% 🔒",
                                        fontSize = 10.sp,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Frame count and speed dashboard
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLight, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "الإطارات المعالجة", fontSize = 9.sp, color = TextSecondary)
                                    Text(
                                        text = "${progress.currentFrame} / ${progress.totalFrames}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "سرعة معالجة الرندرة", fontSize = 9.sp, color = TextSecondary)
                                    Text(
                                        text = String.format("%.0f إطار/ثانية", progress.speedFps),
                                        color = SuccessGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "حجم الملف المتولد", fontSize = 9.sp, color = TextSecondary)
                                    Text(
                                        text = String.format("%.1f MB", progress.outputSizeMb),
                                        color = PremiereOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Cooling indicator
                            if (uiState.isLowCpuExportEnabled) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AcUnit, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                                    Text(text = "نظام تنظيم حرارة المعالج الذكي مفعّل ومستقر", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Mode 3: Success Export State
                    if (uiState.exportFinished) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "تمت عملية تقطيع وتصدير الفيديو بنجاح فائق!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            // Show list of generated files
                            Text(
                                text = "الملفات المحفوظة في ذاكرة الهاتف مباشرة:",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                uiState.processedFiles.forEachIndexed { index, file ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SurfaceLight, RoundedCornerShape(6.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VideoFile,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (file.name.isNotEmpty()) file.name else "قطع_جزء_${index + 1}.mp4",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = if (uiState.isCompressionEnabled) {
                                                String.format("%.1f MB (مضغوط)", (progress?.outputSizeMb ?: 15.0) / uiState.processedFiles.size)
                                            } else {
                                                "الحجم الأصلي دقة 4K"
                                            },
                                            color = SuccessGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = PremiereBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("dismiss_success_dialog_button")
                            ) {
                                Text(text = "العودة لمساحة العمل والتايملاين", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
