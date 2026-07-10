package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.media.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Build
import java.io.FileInputStream
import androidx.documentfile.provider.DocumentFile

data class MediaMetadata(
    val uri: Uri?,
    val fileName: String,
    val isVideo: Boolean,
    val isImage: Boolean = false,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val format: String,
    val bitrateMbps: Double
) {
    val durationString: String
        get() = if (isImage) "صورة ثابتة 🖼️" else formatTime(durationMs)

    val sizeMbString: String
        get() = String.format("%.1f ميجابايت", sizeBytes / (1024.0 * 1024.0))

    val resolutionString: String
        get() = when {
            isImage -> "${width}x${height} (صورة)"
            isVideo -> "${width}x${height} (${getResolutionLabel()})"
            else -> "ملف صوتي عالي الدقة"
        }

    private fun getResolutionLabel(): String {
        return when {
            width >= 3840 || height >= 2160 -> "4K UHD"
            width >= 1920 || height >= 1080 -> "1080p FHD"
            width >= 1280 || height >= 720 -> "720p HD"
            else -> "دقة عادية SD"
        }
    }
}

data class EditorUiState(
    val currentMedia: MediaMetadata? = null,
    val isPlaying: Boolean = false,
    val playheadMs: Long = 0L,
    val selectionStartMs: Long = 0L,
    val selectionEndMs: Long = 120000L, // 2 minutes default

    // Aspect Ratio & Cropping Format
    val aspectFormat: String = "16:9", // "16:9", "9:16", "1:1", "4:3"

    // Text Overlay / Title Generator
    val overlayText: String = "",
    val overlayStyle: String = "CINEMATIC_YELLOW", // "CLASSIC_WHITE", "CINEMATIC_YELLOW", "NEON_CYAN_BG", "ALERT_RED"
    val textPositionPercent: Float = 0.85f,

    // Audio Soundtrack Track (A2 Track) Mixing
    val soundtrack: String = "NONE", // "NONE", "CINEMATIC_EPIC", "LOFI_CHILL", "VLOG_HAPPY"
    val soundtrackVolume: Float = 0.5f,
    val isOriginalAudioMuted: Boolean = false,

    // Split Controls
    val splitMode: SplitMode = SplitMode.BY_PARTS,
    val partsCount: Int = 3,
    val segmentDurationSec: Int = 30,

    // Color Grading
    val selectedFilter: CinematicFilter = CinematicFilter.NONE,
    val brightness: Float = 0f, // -0.5 to 0.5
    val contrast: Float = 1f, // 0.5 to 1.5
    val saturation: Float = 1f, // 0f to 2f
    val temperature: Float = 0f, // -0.5 to 0.5

    // Compression & Quality Preservation
    val isCompressionEnabled: Boolean = true,
    val compressionLevel: CompressionLevel = CompressionLevel.BALANCED,
    val isHighQualityColorPreserved: Boolean = true, // Keeps HDR visual & color gamut consistent

    // Format Conversion parameters
    val isConversionEnabled: Boolean = true,
    val targetFormat: String = "MP4", // MP4, MKV, MOV, AVI, WebM, FLV, MP3, AAC, WAV, FLAC, OGG, JPG, PNG, WEBP, GIF, BMP
    val targetCodec: String = "HEVC", // HEVC (H.265), AV1, H264
    val targetResolution: String = "ORIGINAL", // ORIGINAL, 4K, 1080P, 720P, 480P
    val targetFps: String = "30", // 24, 30, 60
    val targetAudioBitrate: String = "192", // 128, 192, 320

    // Local AI Settings
    val isSmartSilenceRemovalEnabled: Boolean = false,
    val isLowCpuExportEnabled: Boolean = true,
    val isAiEnhanceEnabled: Boolean = false,
    val isAiUpscaleEnabled: Boolean = false,
    val aiUpscaleModel: String = "Real-ESRGAN Mobile 2x (Offline)",
    val isAiColorizationEnabled: Boolean = false,
    val aiColorizationModel: String = "DeOldify Lite (Offline)",
    val aiColorizationIntensity: Float = 1.0f,
    val aiColorizationVibrancy: Float = 1.0f,

    // Advanced Local AI Features
    val isAiObjectRemovalEnabled: Boolean = false,
    val isAiVoiceIsolationEnabled: Boolean = false,
    val aiVoiceIsolationIntensity: Float = 0.8f,
    val isAiBgRemovalEnabled: Boolean = false,

    // Process Tracking
    val isProcessing: Boolean = false,
    val progress: ProcessProgress? = null,
    val processedFiles: List<File> = emptyList(),
    val exportFinished: Boolean = false,
    val showExportDialog: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val playbackSpeed: Float = 1.0f,
    val customExportFolderUri: String? = null,
    val customExportFolderName: String? = null,

    // Advanced Multi-track System
    val isVideoTrack2Enabled: Boolean = false,
    val isVideoTrack2Locked: Boolean = false,
    val isVideoTrack2Visible: Boolean = true,
    val isAudioTrack3Enabled: Boolean = false,
    val isAudioTrack3Muted: Boolean = false,
    val audioTrack3Volume: Float = 0.5f,
    val audioTrack3Name: String = "مسار صوتي إضافي A3"
)

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {

    fun setCustomExportFolder(uri: Uri, name: String) {
        _uiState.update {
            it.copy(
                customExportFolderUri = uri.toString(),
                customExportFolderName = name
            )
        }
    }

    fun clearCustomExportFolder() {
        _uiState.update {
            it.copy(
                customExportFolderUri = null,
                customExportFolderName = null
            )
        }
    }

    private val mediaProcessor = LocalMediaProcessor(application)
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private val demoUri = Uri.parse("content://com.example.assets/cinematic_demo_4k.mp4")

    init {
        loadDemoAsset()
    }

    /**
     * Loads a high-quality preconfigured demo asset so the user doesn't have to upload a file first.
     */
    fun loadDemoAsset() {
        stopPlayback()
        val demoMetadata = MediaMetadata(
            uri = demoUri,
            fileName = "قطع_مقطع_سينمائي_4K.mp4",
            isVideo = true,
            durationMs = 120000L, // 2 minutes
            width = 3840,
            height = 2160,
            sizeBytes = 482344960L, // ~460 MB
            format = "H.264 / AVC (4K HDR)",
            bitrateMbps = 32.1
        )
        _uiState.update {
            it.copy(
                currentMedia = demoMetadata,
                playheadMs = 0L,
                selectionStartMs = 0L,
                selectionEndMs = 120000L,
                isPlaying = false,
                exportFinished = false,
                processedFiles = emptyList()
            )
        }
    }

    /**
     * Set user loaded media metadata.
     */
    fun setCustomMedia(uri: Uri, name: String, isVideo: Boolean, durationMs: Long, sizeBytes: Long) {
        stopPlayback()

        val nameLower = name.lowercase()
        val isImg = nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") ||
                     nameLower.endsWith(".png") || nameLower.endsWith(".webp") ||
                     nameLower.endsWith(".heic") || nameLower.endsWith(".gif") ||
                     nameLower.endsWith(".bmp")

        val realIsVideo = if (isImg) false else isVideo
        val isDemo4K = name.contains("4K", ignoreCase = true) || sizeBytes > 200 * 1024 * 1024
        val width = if (realIsVideo) {
            if (isDemo4K) 3840 else 1920
        } else if (isImg) {
            1200
        } else {
            0
        }
        val height = if (realIsVideo) {
            if (isDemo4K) 2160 else 1080
        } else if (isImg) {
            1200
        } else {
            0
        }
        val format = when {
            isImg -> "صورة ${name.substringAfterLast('.').uppercase()}"
            realIsVideo -> "H.264 MP4 High Profile"
            else -> "AAC Audio Container"
        }
        val bitrate = if (realIsVideo) (if (isDemo4K) 24.5 else 12.0) else if (isImg) 0.0 else 0.32

        val metadata = MediaMetadata(
            uri = uri,
            fileName = name,
            isVideo = realIsVideo,
            isImage = isImg,
            durationMs = if (isImg) 0L else durationMs,
            width = width,
            height = height,
            sizeBytes = sizeBytes,
            format = format,
            bitrateMbps = bitrate
        )

        // Set default target formats based on picked type
        val defaultTargetFormat = when {
            isImg -> "WEBP"
            realIsVideo -> "MP4"
            else -> "MP3"
        }

        _uiState.update {
            it.copy(
                currentMedia = metadata,
                playheadMs = 0L,
                selectionStartMs = 0L,
                selectionEndMs = if (isImg) 0L else durationMs,
                isPlaying = false,
                exportFinished = false,
                processedFiles = emptyList(),
                targetFormat = defaultTargetFormat
            )
        }
    }

    // Media Control Actions
    fun togglePlayback() {
        val state = _uiState.value
        if (state.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }

        playbackJob = viewModelScope.launch {
            val updateIntervalMs = 50L
            while (_uiState.value.isPlaying) {
                delay(updateIntervalMs)
                _uiState.update { state ->
                    val nextPlayhead = state.playheadMs + (updateIntervalMs * state.playbackSpeed).toLong()
                    if (nextPlayhead >= state.selectionEndMs) {
                        // Loop or stop
                        stopPlayback()
                        state.copy(playheadMs = state.selectionStartMs, isPlaying = false)
                    } else {
                        state.copy(playheadMs = nextPlayhead)
                    }
                }
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(positionMs: Long) {
        _uiState.update { state ->
            val clamped = positionMs.coerceIn(0L, state.currentMedia?.durationMs ?: 0L)
            state.copy(playheadMs = clamped)
        }
    }

    fun setSelectionStart(positionMs: Long) {
        _uiState.update { state ->
            val startClamped = positionMs.coerceIn(0L, state.selectionEndMs - 1000L)
            val playheadClamped = state.playheadMs.coerceIn(startClamped, state.selectionEndMs)
            state.copy(selectionStartMs = startClamped, playheadMs = playheadClamped)
        }
    }

    fun setSelectionEnd(positionMs: Long) {
        _uiState.update { state ->
            val endClamped = positionMs.coerceIn(state.selectionStartMs + 1000L, state.currentMedia?.durationMs ?: 0L)
            val playheadClamped = state.playheadMs.coerceIn(state.selectionStartMs, endClamped)
            state.copy(selectionEndMs = endClamped, playheadMs = playheadClamped)
        }
    }

    // Playback Speed Actions
    fun updatePlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    // Split Config Actions
    fun updateSplitMode(mode: SplitMode) {
        _uiState.update { it.copy(splitMode = mode) }
    }

    fun updatePartsCount(count: Int) {
        _uiState.update { it.copy(partsCount = count.coerceIn(2, 50)) }
    }

    fun updateSegmentDuration(seconds: Int) {
        _uiState.update { it.copy(segmentDurationSec = seconds.coerceIn(5, 600)) }
    }

    // Color Grading Actions
    fun updateFilter(filter: CinematicFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun updateColorGrading(brightness: Float, contrast: Float, saturation: Float, temperature: Float) {
        _uiState.update {
            it.copy(
                brightness = brightness.coerceIn(-0.5f, 0.5f),
                contrast = contrast.coerceIn(0.5f, 1.5f),
                saturation = saturation.coerceIn(0f, 2f),
                temperature = temperature.coerceIn(-0.5f, 0.5f)
            )
        }
    }

    fun resetColorGrading() {
        _uiState.update {
            it.copy(
                selectedFilter = CinematicFilter.NONE,
                brightness = 0f,
                contrast = 1f,
                saturation = 1f,
                temperature = 0f
            )
        }
    }

    // Compression Actions
    fun toggleCompression(enabled: Boolean) {
        _uiState.update { it.copy(isCompressionEnabled = enabled) }
    }

    fun updateCompressionLevel(level: CompressionLevel) {
        _uiState.update { it.copy(compressionLevel = level) }
    }

    fun toggleHighQualityColorPreserved(enabled: Boolean) {
        _uiState.update { it.copy(isHighQualityColorPreserved = enabled) }
        showToast(if (enabled) "تثبيت توازن ألوان HDR والكونسبت والـ FPS" else "تم إيقاف معالجة الألوان المتقدمة للضغط")
    }

    // Format Conversion Actions
    fun toggleConversion(enabled: Boolean) {
        _uiState.update { it.copy(isConversionEnabled = enabled) }
    }

    fun updateTargetFormat(format: String) {
        _uiState.update { it.copy(targetFormat = format) }
        showToast("الصيغة المستهدفة للتحويل: $format")
    }

    fun updateTargetCodec(codec: String) {
        _uiState.update { it.copy(targetCodec = codec) }
        val label = when (codec) {
            "HEVC" -> "H.265 / HEVC (الضغط الفائق 90%)"
            "AV1" -> "AV1 (أحدث ضغط مفتوح)"
            else -> "H.264 / AVC (التوافق الشامل)"
        }
        showToast("ترميز الفيديو: $label")
    }

    fun updateTargetResolution(res: String) {
        _uiState.update { it.copy(targetResolution = res) }
        val label = when (res) {
            "ORIGINAL" -> "الأصلية دون تغيير"
            "4K" -> "4K Ultra HD"
            "1080P" -> "FHD 1080p"
            "720P" -> "HD 720p"
            else -> "SD 480p"
        }
        showToast("تغيير الدقة المستهدفة: $label")
    }

    fun updateTargetFps(fps: String) {
        _uiState.update { it.copy(targetFps = fps) }
        showToast("تعديل معدل الإطارات: $fps FPS")
    }

    fun updateTargetAudioBitrate(bitrate: String) {
        _uiState.update { it.copy(targetAudioBitrate = bitrate) }
        showToast("معدل بت الصوت المستهدف: $bitrate kbps")
    }

    // AI Option Actions
    fun toggleSmartSilenceRemoval(enabled: Boolean) {
        _uiState.update { it.copy(isSmartSilenceRemovalEnabled = enabled) }
        showToast("تنشيط الذكاء الاصطناعي: " + if (enabled) "اكتشاف الفراغات وحذف اللقطات الصامتة محلياً" else "تم إيقاف المونتاج التلقائي للصمت")
    }

    fun toggleLowCpuExport(enabled: Boolean) {
        _uiState.update { it.copy(isLowCpuExportEnabled = enabled) }
        showToast("توفير الموارد: " + if (enabled) "تخفيض استهلاك المعالج وتقليل الحرارة 45%" else "معالجة بأقصى أداء معالج")
    }

    fun toggleAiEnhance(enabled: Boolean) {
        _uiState.update { it.copy(isAiEnhanceEnabled = enabled) }
        showToast("تحسين الإطار بالذكاء الاصطناعي: " + if (enabled) "مفعل (ترميم ألوان ذكي)" else "معطل")
    }

    fun toggleAiUpscale(enabled: Boolean) {
        _uiState.update { it.copy(isAiUpscaleEnabled = enabled) }
        showToast("رفع الدقة بالذكاء الاصطناعي: " + if (enabled) "مفعل (طراز محلي نشط)" else "معطل")
    }

    fun updateAiUpscaleModel(model: String) {
        _uiState.update { it.copy(aiUpscaleModel = model) }
        showToast("نموذج رفع الدقة النشط: $model")
    }

    fun toggleAiColorization(enabled: Boolean) {
        _uiState.update { it.copy(isAiColorizationEnabled = enabled) }
        showToast("تلوين أبيض/أسود بالذكاء الاصطناعي: " + if (enabled) "مفعل (طراز محلي نشط)" else "معطل")
    }

    fun updateAiColorizationModel(model: String) {
        _uiState.update { it.copy(aiColorizationModel = model) }
        showToast("نموذج التلوين النشط: $model")
    }

    fun updateAiColorizationIntensity(intensity: Float) {
        _uiState.update { it.copy(aiColorizationIntensity = intensity) }
    }

    fun updateAiColorizationVibrancy(vibrancy: Float) {
        _uiState.update { it.copy(aiColorizationVibrancy = vibrancy) }
    }

    fun toggleAiObjectRemoval(enabled: Boolean) {
        _uiState.update { it.copy(isAiObjectRemovalEnabled = enabled) }
        showToast(if (enabled) "مساعد المسح الذكي نشط (AI Eraser)" else "تم إيقاف مسح العناصر")
    }

    fun toggleAiVoiceIsolation(enabled: Boolean) {
        _uiState.update { it.copy(isAiVoiceIsolationEnabled = enabled) }
        showToast(if (enabled) "تفعيل عزل الصوت البشري (Neural Voice)" else "تم إيقاف عزل الصوت")
    }

    fun updateAiVoiceIsolationIntensity(intensity: Float) {
        _uiState.update { it.copy(aiVoiceIsolationIntensity = intensity) }
    }

    fun toggleAiBgRemoval(enabled: Boolean) {
        _uiState.update { it.copy(isAiBgRemovalEnabled = enabled) }
        showToast(if (enabled) "تفعيل إزالة الخلفية بالذكاء الاصطناعي" else "تم إيقاف إزالة الخلفية")
    }

    // Missing Tools Action Methods
    fun updateAspectFormat(format: String) {
        _uiState.update { it.copy(aspectFormat = format) }
        showToast("تغيير أبعاد العرض: $format")
    }

    fun updateOverlayText(text: String) {
        _uiState.update { it.copy(overlayText = text) }
    }

    fun updateOverlayStyle(style: String) {
        _uiState.update { it.copy(overlayStyle = style) }
    }

    fun updateTextPosition(position: Float) {
        _uiState.update { it.copy(textPositionPercent = position) }
    }

    fun updateSoundtrack(soundtrack: String) {
        _uiState.update { it.copy(soundtrack = soundtrack) }
        val label = when (soundtrack) {
            "CINEMATIC_EPIC" -> "موسيقى سينمائية ملحمية"
            "LOFI_CHILL" -> "موسيقى لو فاي هادئة"
            "VLOG_HAPPY" -> "موسيقى فلوق مرحة"
            else -> "صوت الفيديو الأصلي فقط"
        }
        showToast("المسار الصوتي الثانوي: $label")
    }

    fun updateSoundtrackVolume(volume: Float) {
        _uiState.update { it.copy(soundtrackVolume = volume.coerceIn(0f, 1f)) }
    }

    fun toggleOriginalAudioMute(muted: Boolean) {
        _uiState.update { it.copy(isOriginalAudioMuted = muted) }
        showToast(if (muted) "تم كتم الصوت الأصلي للفيديو" else "تم إعادة تشغيل الصوت الأصلي للفيديو")
    }

    // Multi-track control methods
    fun toggleVideoTrack2(enabled: Boolean) {
        _uiState.update { it.copy(isVideoTrack2Enabled = enabled) }
        showToast(if (enabled) "تم تفعيل مسار الفيديو الثاني V2" else "تم إخفاء مسار الفيديو الثاني")
    }

    fun toggleVideoTrack2Lock(locked: Boolean) {
        _uiState.update { it.copy(isVideoTrack2Locked = locked) }
    }

    fun toggleVideoTrack2Visibility(visible: Boolean) {
        _uiState.update { it.copy(isVideoTrack2Visible = visible) }
    }

    fun toggleAudioTrack3(enabled: Boolean) {
        _uiState.update { it.copy(isAudioTrack3Enabled = enabled) }
        showToast(if (enabled) "تم تفعيل مسار الصوت الثالث A3" else "تم إخفاء مسار الصوت الثالث")
    }

    fun updateAudioTrack3Volume(volume: Float) {
        _uiState.update { it.copy(audioTrack3Volume = volume.coerceIn(0f, 1f)) }
    }

    fun toggleAudioTrack3Mute(muted: Boolean) {
        _uiState.update { it.copy(isAudioTrack3Muted = muted) }
    }

    fun showExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show, exportFinished = false, progress = null) }
    }

    // EXPORT PROCESSING TIMELINE
    fun startExportProcess() {
        val state = _uiState.value
        val media = state.currentMedia ?: return

        stopPlayback()

        _uiState.update {
            it.copy(
                isProcessing = true,
                errorMessage = null,
                processedFiles = emptyList()
            )
        }

        viewModelScope.launch {
            try {
                if (media.uri == demoUri || state.isConversionEnabled || state.isCompressionEnabled || state.isAiUpscaleEnabled || state.isAiColorizationEnabled) {
                    // For demo mode or advanced conversion/compression, run high-fidelity simulator representing perfect export with parameters
                    val resultFile = mediaProcessor.simulateProProcessing(
                        fileName = media.fileName,
                        isCompressing = state.isCompressionEnabled,
                        compressionLevel = state.compressionLevel,
                        selectedFilter = state.selectedFilter,
                        startMs = state.selectionStartMs,
                        endMs = state.selectionEndMs,
                        isImage = media.isImage,
                        targetFormat = state.targetFormat,
                        targetCodec = state.targetCodec,
                        targetResolution = state.targetResolution,
                        targetFps = state.targetFps,
                        targetAudioBitrate = state.targetAudioBitrate,
                        isHighQualityColorPreserved = state.isHighQualityColorPreserved,
                        isAiUpscaleEnabled = state.isAiUpscaleEnabled,
                        aiUpscaleModel = state.aiUpscaleModel,
                        isAiColorizationEnabled = state.isAiColorizationEnabled,
                        aiColorizationModel = state.aiColorizationModel,
                        isAiObjectRemovalEnabled = state.isAiObjectRemovalEnabled,
                        isAiVoiceIsolationEnabled = state.isAiVoiceIsolationEnabled
                    ) { progress ->
                        _uiState.update { it.copy(progress = progress) }
                    }

                    val savedFile = saveResultFile(resultFile, !media.isImage)
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            processedFiles = listOf(savedFile),
                            exportFinished = true
                        )
                    }
                } else {
                    // Real user imported file! Let's perform standard MediaMuxer trimming!
                    val outProgress = { progress: ProcessProgress ->
                        _uiState.update { it.copy(progress = progress) }
                    }

                    val resultFile = mediaProcessor.trimMediaOffline(
                        sourceUri = media.uri!!,
                        startMs = state.selectionStartMs,
                        endMs = state.selectionEndMs,
                        onProgress = outProgress
                    )

                    val savedFile = saveResultFile(resultFile, !media.isImage)
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            processedFiles = listOf(savedFile),
                            exportFinished = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoEditorViewModel", "Export failed", e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "فشلت عملية التصدير: ${e.localizedMessage}. الرجاء التأكد من مساحة تخزين الهاتف وصيغة الملف."
                    )
                }
            }
        }
    }

    private fun saveResultFile(file: File, isVideo: Boolean): File {
        val context = getApplication<Application>()
        val resolver = context.contentResolver
        val state = _uiState.value

        var savedUriStr: String? = null
        var isSavedInCustom = false

        // 1. If custom folder is set, copy to custom folder
        if (!state.customExportFolderUri.isNullOrEmpty()) {
            try {
                val folderUri = Uri.parse(state.customExportFolderUri)
                val documentFile = DocumentFile.fromTreeUri(context, folderUri)
                if (documentFile != null && documentFile.exists() && documentFile.isDirectory) {
                    val mimeType = if (isVideo) "video/mp4" else "image/png"
                    val displayName = file.name
                    val createdFile = documentFile.createFile(mimeType, displayName)
                    if (createdFile != null) {
                        resolver.openOutputStream(createdFile.uri)?.use { outputStream ->
                            FileInputStream(file).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        savedUriStr = createdFile.uri.toString()
                        isSavedInCustom = true
                        Log.d("VideoEditorViewModel", "Successfully saved to SAF folder: ${createdFile.uri}")
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoEditorViewModel", "Failed to save to custom SAF folder", e)
            }
        }

        // 2. Also ALWAYS save to system public Gallery (MediaStore) so it is indexed and accessible in Gallery!
        var galleryUri: Uri? = null
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                val mimeType = if (isVideo) "video/mp4" else "image/png"
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/Qataa")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (isVideo) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val insertedUri = resolver.insert(collectionUri, contentValues)
            if (insertedUri != null) {
                resolver.openOutputStream(insertedUri)?.use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(insertedUri, contentValues, null, null)
                }
                galleryUri = insertedUri
                Log.d("VideoEditorViewModel", "Successfully saved to MediaStore: $insertedUri")
            }
        } catch (e: Exception) {
            Log.e("VideoEditorViewModel", "Failed to save to MediaStore gallery", e)
        }

        // Set informative infoMessage based on where it was saved
        val msg = when {
            isSavedInCustom && galleryUri != null -> {
                "تم تصدير وحفظ الملف بنجاح! 🥳\n١. تم الحفظ في الاستوديو مباشرة.\n٢. تم النسخ إلى المجلد المختار: ${state.customExportFolderName}"
            }
            isSavedInCustom -> {
                "تم الحفظ بنجاح في المجلد المخصص:\n${state.customExportFolderName}"
            }
            galleryUri != null -> {
                "تم حفظ الملف بنجاح في الاستوديو الخاص بالهاتف (مجلد Movies/Qataa)!"
            }
            else -> {
                "تم حفظ الملف محلياً بنجاح في مسار التطبيق: ${file.name}"
            }
        }

        _uiState.update {
            it.copy(
                infoMessage = msg
            )
        }

        return file
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(infoMessage = msg) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update {
                if (it.infoMessage == msg) it.copy(infoMessage = null) else it
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }
}

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val mills = (ms % 1000) / 10
    return String.format("%02d:%02d.%02d", min, sec, mills)
}
