package com.example.model

enum class ModelDownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    LOADED
}

data class LocalAiModel(
    val id: String,
    val name: String,
    val parameterCount: String,
    val format: String = "GGUF",
    val quantization: String = "Q4_K_M",
    val fileSizeBytes: Long,
    val description: String,
    val downloadUrl: String,
    val status: ModelDownloadStatus = ModelDownloadStatus.NOT_DOWNLOADED,
    val downloadProgressPercent: Int = 0,
    val memoryUsageMb: Int = 0,
    val recommendedRamGb: Int = 4,
    val tags: List<String> = emptyList()
) {
    val formattedSize: String
        get() {
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1000) {
                String.format(java.util.Locale.US, "%.1f GB", mb / 1024.0)
            } else {
                String.format(java.util.Locale.US, "%.0f MB", mb)
            }
        }
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "User" or model name
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val tokensGenerated: Int = 0,
    val tokensPerSecond: Float = 0f
)
