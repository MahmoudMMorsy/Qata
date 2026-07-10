package com.example.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.media.CinematicFilter
import com.example.media.CompressionLevel
import com.example.media.SplitMode
import com.example.ui.theme.*
import com.example.viewmodel.EditorUiState
import com.example.ui.components.*
import com.example.viewmodel.MediaMetadata
import com.example.viewmodel.VideoEditorViewModel
import com.example.viewmodel.formatTime
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showSplashScreen by remember { mutableStateOf(true) }
    var splashProgress by remember { mutableStateOf(0f) }
    var activeStatusIndex by remember { mutableStateOf(0) }
    var selectedTabIdx by remember { mutableStateOf(0) }
    var workspaceMode by remember { mutableStateOf(0) } // 0: Premiere, 1: AI, 2: Cinema/Scopes
    var activeNavTab by remember { mutableStateOf("EDITOR") }

    val statusTexts = listOf(
        "جاري فحص محرك المونتاج الذكي قَطْع v3.5...",
        "التحقق من مسرع رسوميات GPU محلياً للتصدير فائق السرعة...",
        "تهيئة مسارات معالجة الفيديو بدقة 4K Ultra HD...",
        "تنسيق المونتاج الفوري وتنشيط تأثيرات الحركة الذكية...",
        "تحضير الفلاتر السينمائية ولوحات الألوان الإبداعية...",
        "جاري تنشيط ميزة الإزالة الذكية للأصوات وتخميد الضوضاء...",
        "تأمين بيئة المونتاج الخالية من النت والإعلانات... جاهز للبدء!"
    )

    // Animated loading progress
    LaunchedEffect(showSplashScreen) {
        if (showSplashScreen) {
            val totalSteps = 100
            for (step in 1..totalSteps) {
                delay(30)
                splashProgress = step / 100f
                if (step % 14 == 0) {
                    activeStatusIndex = (activeStatusIndex + 1) % statusTexts.size
                }
            }
            showSplashScreen = false
        }
    }

    // File Picker for offline video & audio files
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            retrieveMediaMetadata(context, selectedUri) { name, isVideo, duration, size ->
                viewModel.setCustomMedia(selectedUri, name, isVideo, duration, size)
            }
        }
    }

    // Target folder picker for custom export directory
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("EditorScreen", "Failed to persist SAF folder permission", e)
            }

            // Get user-friendly name of the chosen folder
            var folderName = "مجلد مخصص"
            try {
                val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                if (docFile != null && docFile.name != null) {
                    folderName = docFile.name!!
                }
            } catch (e: Exception) {
                // Keep default
            }
            viewModel.setCustomExportFolder(uri, folderName)
        }
    }

    // Handle incoming snackbar/info messages
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            delay(3000)
            viewModel.dismissMessage()
        }
    }

    if (showSplashScreen) {
        // IMMERSIVE CINEMATIC SPLASH SCREEN (Adobe Premiere / DaVinci Style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0D))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                // Wide Cinematic Logo Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_qataa_splash_1783284037961),
                        contentDescription = "بانر البدء",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.6f), TrackVideoColor.copy(alpha = 0.6f)))),
                                RoundedCornerShape(16.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )

                    // Translucent Badge Overlay at Top Left
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "الإصدار ٣.٥ الممتاز ✨",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Skip button in Splash
                    TextButton(
                        onClick = { showSplashScreen = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text(text = "تخطي التحميل ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App info headers
                Text(
                    text = "قَطْع — مُعالج ومُحرر الفيديو والذكاء الاصطناعي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "برنامج تحرير فيديو احترافي ومحلي ١٠٠٪ بدون إنترنت",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // High-fidelity progress indicator with custom neon glow background
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current progress percent
                        Text(
                            text = "${(splashProgress * 100).toInt()}%",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )

                        // Action status Text
                        Text(
                            text = "جاري التحميل...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Neon bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF131722), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(splashProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(TrackVideoColor, NeonCyan)
                                    ),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Running dynamic task subtitle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = statusTexts[activeStatusIndex],
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "status_anim"
                        ) { text ->
                            Text(
                                text = text,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Capabilities indicators footer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CapabilityBadge(icon = Icons.Default.Bolt, label = "معالج GPU محلي")
                    Box(modifier = Modifier.size(4.dp).background(TextSecondary, CircleShape))
                    CapabilityBadge(icon = Icons.Default.Build, label = "الذكاء الاصطناعي")
                    Box(modifier = Modifier.size(4.dp).background(TextSecondary, CircleShape))
                    CapabilityBadge(icon = Icons.Default.Lock, label = "آمن ١٠٠٪")
                }
            }
        }
    } else {
        // MAIN WORKSPACE INTERFACE
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Img Avatar Logo from the user's gorgeous icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonCyan, TrackVideoColor))),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .shadow(6.dp, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.img_qataa_logo_1783284023095),
                                    contentDescription = "شعار قطع",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column {
                                Text(
                                    text = "قَطْع الاحترافي",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "محرر ومعالج 4K الذكي محلياً",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        // Fully local offline status tag
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            contentColor = SuccessGreen,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(6.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Text(
                                    text = "مسرع GPU نشط",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Reset button
                        IconButton(
                            onClick = { viewModel.loadDemoAsset() },
                            modifier = Modifier.testTag("reset_demo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحميل الملف التجريبي",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSlateBackground,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = DarkSlateBackground,
            bottomBar = {
                Column {
                    if (activeNavTab == "EDITOR") {
                        BottomExportBar(
                            metadata = uiState.currentMedia,
                            onExportClick = { viewModel.showExportDialog(true) }
                        )
                    }
                    CyberpunkBottomNavBar(
                        activeTab = activeNavTab,
                        onTabSelected = { activeNavTab = it }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (activeNavTab) {
                    "EDITOR" -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. FIXED PREVIEW MONITOR (Always visible at the top, DaVinci style)
                            PreviewMonitorWithCinemaScopes(
                                uiState = uiState,
                                cinemaMode = (workspaceMode == 2),
                                onTogglePlay = { viewModel.togglePlayback() },
                                onSeek = { viewModel.seekTo(it) }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // 2. SCROLLABLE EDITING CONTROL WORKSPACE (Underneath)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp)
                            ) {
                                // Workspace selector chips
                                WorkspaceSelectorChips(
                                    currentMode = workspaceMode,
                                    onModeChange = { mode ->
                                        workspaceMode = mode
                                        // Sync workspace mode to tab indices automatically!
                                        when (mode) {
                                            0 -> selectedTabIdx = 0 // Cut / splits
                                            1 -> selectedTabIdx = 4 // AI controls
                                            2 -> selectedTabIdx = 1 // Color filters
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Timeline Tracks (Trimming and Waveforms)
                                TimelineTracks(
                                    uiState = uiState,
                                    onSeek = { viewModel.seekTo(it) },
                                    onStartChange = { viewModel.setSelectionStart(it) },
                                    onEndChange = { viewModel.setSelectionEnd(it) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Workspace Control Tabs
                                WorkspaceControlTabs(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    selectedTabIdx = selectedTabIdx,
                                    onTabSelected = { selectedTabIdx = it },
                                    workspaceMode = workspaceMode
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Collapsible Source Info Panel
                                SourceFileSelector(
                                    metadata = uiState.currentMedia,
                                    onPickFile = { mediaPickerLauncher.launch("video/*, audio/*") },
                                    onLoadDemo = { viewModel.loadDemoAsset() }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    "HOME" -> {
                        HomeView(uiState, viewModel, onStartEditing = { activeNavTab = "EDITOR" })
                    }
                    "PROJECTS" -> {
                        ProjectsView(uiState, viewModel, onOpenProject = { activeNavTab = "EDITOR" })
                    }
                    "RECORD" -> {
                        RecordView(uiState, viewModel)
                    }
                    "STUDIO" -> {
                        StudioView(uiState, viewModel, onOpenProject = { activeNavTab = "EDITOR" })
                    }
                }

                // Floating Custom Elegant Message/Toast Overlay
                AnimatedVisibility(
                    visible = uiState.infoMessage != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Surface(
                        color = SurfaceLight,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 6.dp,
                        border = BorderStroke(1.dp, PremiereBlue.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "تنبيه",
                                tint = NeonCyan
                            )
                            Text(
                                text = uiState.infoMessage ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Export Processing Progress Modal Dialog
            if (uiState.showExportDialog) {
                ExportProgressDialog(
                    uiState = uiState,
                    onDismiss = { viewModel.showExportDialog(false) },
                    onStartExport = { viewModel.startExportProcess() },
                    onChooseFolder = { folderPickerLauncher.launch(null) },
                    onClearFolder = { viewModel.clearCustomExportFolder() }
                )
            }
        }
    }
}

@Composable
fun HomeView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel,
    onStartEditing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E14)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonCyan, NeonPink))),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "NEO-EDIT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonPink.copy(alpha = 0.5f),
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(x = (-2).dp, y = (1).dp)
                    )
                    Text(
                        text = "NEO-EDIT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan.copy(alpha = 0.5f),
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(x = (2).dp, y = (-1).dp)
                    )
                    Text(
                        text = "NEO-EDIT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "مرحباً بك في مستقبل معالجة المونتاج المحلي والذكي",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onStartEditing,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "ابدأ مونتاج فيديو جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Capabilities Grid
        Text(
            text = "المميزات النشطة محلياً",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(SurfaceSlate, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, Color(0xFF161B29)), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.SdCard, contentDescription = null, tint = NeonCyan)
                    Text(text = "معالجة بلا حدود", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "تعديل، قص، وتصدير غير محدود بجودة فائقة.", color = TextSecondary, fontSize = 10.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(SurfaceSlate, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, Color(0xFF161B29)), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = NeonPink)
                    Text(text = "صوت ذكي محلي", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "إزالة الضوضاء وتضخيم الصوت بالكامل دون إنترنت.", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceSlate, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF161B29)), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(text = "ضغط الفيديو الذكي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "تقليل الحجم لأكثر من ٨٠٪ مع الحفاظ التام على الدقة الأصلية.", color = TextSecondary, fontSize = 11.sp)
                }
                Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
fun ProjectsView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel,
    onOpenProject: () -> Unit
) {
    val sampleProjects = listOf(
        Pair("مقطع النيون الافتراضي.mp4", "منذ ساعتين • 24.5MB"),
        Pair("تسجيل كاميرا السيارة الاحترافي.mov", "أمس • 140.2MB"),
        Pair("مؤثرات الصوت السايكوديلية.mp3", "منذ ٣ أيام • 4.8MB"),
        Pair("تصميم مقدمة يوتيوب بدقة 4K.mp4", "الأسبوع الماضي • 92.1MB")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "المشاريع الأخيرة والمحفوظات",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        sampleProjects.forEach { proj ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                border = BorderStroke(1.dp, Color(0xFF161B29)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProject() }
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = TextSecondary)

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(text = proj.first, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = proj.second, color = TextSecondary, fontSize = 10.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.3f), NeonPink.copy(alpha = 0.3f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordTimeSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordTimeSeconds++
            }
        } else {
            recordTimeSeconds = 0
        }
    }

    val minutes = recordTimeSeconds / 60
    val seconds = recordTimeSeconds % 60
    val timerStr = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "تسجيل واستيراد الكاميرا الذكية",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        // Camera Simulated Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(2.dp, if (isRecording) NeonPink else NeonCyan), RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Lens simulator / circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                // Grid lines
                drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(w / 3f, 0f), end = Offset(w / 3f, h))
                drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(2 * w / 3f, 0f), end = Offset(2 * w / 3f, h))
                drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(0f, h / 3f), end = Offset(w, h / 3f))
                drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(0f, 2 * h / 3f), end = Offset(w, 2 * h / 3f))

                // Outer and inner target circles
                drawCircle(color = if (isRecording) NeonPink.copy(alpha = 0.1f) else NeonCyan.copy(alpha = 0.1f), radius = 50.dp.toPx(), center = center)
                drawCircle(color = if (isRecording) NeonPink.copy(alpha = 0.3f) else NeonCyan.copy(alpha = 0.3f), radius = 10.dp.toPx(), center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
            }

            // REC Blinking dot and time duration
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isRecording) {
                    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "blink"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = alpha))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
                Text(
                    text = if (isRecording) "REC $timerStr" else "READY",
                    color = if (isRecording) NeonPink else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Specs Overlay
            Text(
                text = "4K UHD 60FPS • ISO 400",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            )

            // Bottom Audio Meter
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "BAT: 74%", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf(5, 8, 12, 10, 6, 9, 11, 4, 7, 3).forEach { ht ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (isRecording) (ht * (1 + Math.random() * 1.5)).dp else ht.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isRecording) NeonPink else NeonCyan)
                        )
                    }
                }
            }
        }

        // Recording Button
        Box(
            modifier = Modifier
                .size(70.dp)
                .border(BorderStroke(4.dp, Color.White), CircleShape)
                .padding(6.dp)
                .clip(CircleShape)
                .background(if (isRecording) NeonPink else Color.Red)
                .clickable { isRecording = !isRecording },
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun StudioView(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel,
    onOpenProject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "استوديو القوالب والمؤثرات الجاهزة",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )

        listOf(
            Triple("قالب ريترو نيون رائع", "Retro Wave Template", NeonPink),
            Triple("مقدمة يوتيوب سينمائية", "Cinematic Intro Preset", NeonCyan),
            Triple("فلاتر تلوين هوليوود", "Hollywood Grading Pack", Color.Yellow),
            Triple("حزمة أصوات السايبربانك", "Cyberpunk SFX Kit", Color(0xFFE91E63))
        ).forEach { pack ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                border = BorderStroke(1.dp, Color(0xFF161B29)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProject() }
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = TextSecondary)

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(text = pack.first, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = pack.second, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(pack.third.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = pack.third)
                    }
                }
            }
        }
    }
}

@Composable
fun CyberpunkBottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    val navItems = listOf(
        NavToolItem("HOME", "الرئيسية", Icons.Default.Home),
        NavToolItem("PROJECTS", "المشاريع", Icons.Default.Folder),
        NavToolItem("RECORD", "التسجيل", Icons.Default.RadioButtonChecked),
        NavToolItem("STUDIO", "المعرض", Icons.Default.Videocam),
        NavToolItem("EDITOR", "المحرر الذكي", Icons.Default.ContentCut)
    )

    Surface(
        color = Color(0xFF06070B),
        border = BorderStroke(1.dp, Color(0xFF141A29)),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isActive = item.key == activeTab
                val color = if (isActive) NeonCyan else TextSecondary

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(item.key)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(3.dp)
                                .background(NeonCyan, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(2.dp),
                                    ambientColor = NeonCyan,
                                    spotColor = NeonCyan
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(7.dp))
                    }

                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = color,
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

data class NavToolItem(val key: String, val label: String, val icon: ImageVector)

@Composable
fun CapabilityBadge(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WorkspaceSelectorChips(
    currentMode: Int,
    onModeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(SurfaceSlate, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, SurfaceLight), RoundedCornerShape(12.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple(0, "المونتاج", Icons.Default.Tune),
            Triple(1, "الذكاء الاصطناعي", Icons.Default.Bolt),
            Triple(2, "موجات السينما", Icons.Default.Tv)
        ).forEach { (idx, label, icon) ->
            val isSelected = currentMode == idx
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            when (idx) {
                                0 -> PremiereBlue.copy(alpha = 0.2f)
                                1 -> NeonCyan.copy(alpha = 0.2f)
                                else -> PremiereOrange.copy(alpha = 0.2f)
                            }
                        } else Color.Transparent
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) {
                                when (idx) {
                                    0 -> PremiereBlue
                                    1 -> NeonCyan
                                    else -> PremiereOrange
                                }
                            } else Color.Transparent
                        ),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onModeChange(idx) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        when (idx) {
                            0 -> PremiereBlue
                            1 -> NeonCyan
                            else -> PremiereOrange
                        }
                    } else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

private fun retrieveMediaMetadata(
    context: Context,
    uri: Uri,
    onResult: (String, Boolean, Long, Long) -> Unit
) {
    try {
        var name = "مقطع_مستورد.mp4"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }

        var isVideo = true
        var duration = 10000L // default fallback 10s
        var size = 0L

        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
            size = afd.length
        }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            isVideo = hasVideo != null && hasVideo == "yes"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durationStr?.toLong() ?: 10000L
        } catch (e: Exception) {
            // fallback if metadata retriever fails due to simulator sandbox
            isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".mov")
        } finally {
            retriever.release()
        }

        onResult(name, isVideo, duration, size)
    } catch (e: Exception) {
        onResult("ملف_محلي_${System.currentTimeMillis().toString().takeLast(4)}", true, 60000L, 50 * 1024 * 1024L)
    }
}

/**
 * 1. File Selector & Source Info Panel Component
 */
@Composable
fun SourceFileSelector(
    metadata: MediaMetadata?,
    onPickFile: () -> Unit,
    onLoadDemo: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مصدر الفيديو / الصوت الحالي",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pick Actual File button
                    OutlinedButton(
                        onClick = onPickFile,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PremiereBlue.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("import_media_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "استيراد",
                            modifier = Modifier.size(16.dp),
                            tint = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "فتح ملف محلي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (metadata != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLight, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (metadata.isVideo) TrackVideoColor.copy(alpha = 0.2f) else TrackAudioColor.copy(
                                    alpha = 0.2f
                                ),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (metadata.isVideo) Icons.Default.VideoLibrary else Icons.Default.AudioFile,
                            contentDescription = "نوع الملف",
                            tint = if (metadata.isVideo) TrackVideoColor else TrackAudioColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = metadata.fileName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = metadata.resolutionString,
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(TextSecondary, CircleShape)
                            )
                            Text(
                                text = metadata.format,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = metadata.durationString,
                            color = PremiereOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = metadata.sizeMbString,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(SurfaceLight, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "الرجاء فتح ملف فيديو أو صوت للبدء في التعديل",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 2. Main Color Grading Monitor / Preview Screen Component
 */
@Composable
@Composable
fun BottomExportBar(
    metadata: MediaMetadata?,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceSlate,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "رابط إخراج المونتاج",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "محلي ⟷ مجلد الاستوديو",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Central Export Button
            Button(
                onClick = onExportClick,
                enabled = metadata != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiereBlue,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceLight,
                    disabledContentColor = TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .testTag("export_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "بدء التصدير السريع 4K",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 6. Export Processing & Complete Progress Tracking Dialog Modal
 */
@OptIn(ExperimentalAnimationApi::class)
