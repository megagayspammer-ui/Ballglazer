package com.example.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.LocalAiManager
import com.example.ai.LocalAiState
import com.example.model.ChatMessage
import com.example.model.LocalAiModel
import com.example.model.ModelDownloadStatus
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.NamedDeviceGreen
import com.example.ui.theme.RadarNeonCyan
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioRose

@Composable
fun LocalAiScreen(
    aiManager: LocalAiManager,
    state: LocalAiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        // Model Status Header
        Surface(
            color = Color(0xFF131722),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StudioIndigo.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = StudioIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = state.activeModel?.name ?: "No Model Loaded",
                            color = DarkTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NamedDeviceGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "On-Device Offline Inference (GGUF)",
                                color = DarkTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0xFF1F293D),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${state.usedModelStorageMb} MB Cached",
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Sub Tabs: [Offline Chat, Model Manager, Parameters]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioDarkSurface,
            contentColor = StudioIndigo,
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Local Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_local_chat")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Model Hub (${state.models.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_model_hub")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_ai_settings")
            )
        }

        // Tab Body
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> {
                    LocalChatTab(
                        messages = state.chatMessages,
                        isGenerating = state.isGenerating,
                        inputText = inputText,
                        onInputChange = { inputText = it },
                        onSendMessage = {
                            aiManager.sendMessage(inputText)
                            inputText = ""
                        },
                        onClearChat = { aiManager.clearChat() }
                    )
                }
                1 -> {
                    ModelHubTab(
                        models = state.models,
                        activeModelId = state.activeModelId,
                        onDownload = { aiManager.startModelDownload(it) },
                        onCancel = { aiManager.cancelDownload(it) },
                        onDelete = { aiManager.deleteDownloadedModel(it) },
                        onSetActive = { aiManager.setActiveModel(it) }
                    )
                }
                2 -> {
                    AiSettingsTab(
                        systemPrompt = state.systemPrompt,
                        temperature = state.temperature,
                        onSystemPromptChange = { aiManager.setSystemPrompt(it) },
                        onTemperatureChange = { aiManager.setTemperature(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalChatTab(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onClearChat: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "User"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser) StudioIndigo else Color(0xFF1A2234),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg.sender,
                                    color = if (isUser) Color.White.copy(alpha = 0.8f) else RadarNeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!isUser && msg.tokensPerSecond > 0f) {
                                    Text(
                                        text = "${msg.tokensPerSecond} t/s",
                                        color = DarkTextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = msg.text,
                                color = DarkTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = StudioIndigo,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Synthesizing tokens locally on NPU/CPU...",
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Input Row
        Surface(
            color = Color(0xFF141C2E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("Ask local model offline...", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioIndigo,
                        unfocusedBorderColor = Color(0xFF26334D),
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("local_ai_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onSendMessage,
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier.testTag("send_ai_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isGenerating) StudioIndigo else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelHubTab(
    models: List<LocalAiModel>,
    activeModelId: String?,
    onDownload: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSetActive: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "DOWNLOADABLE ON-DEVICE MODELS",
                color = DarkTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        items(models, key = { it.id }) { model ->
            val isActive = model.id == activeModelId

            Surface(
                color = Color(0xFF141C2E),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isActive) NamedDeviceGreen.copy(alpha = 0.5f) else Color(0xFF26334D),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = model.name,
                                    color = DarkTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACTIVE",
                                        color = NamedDeviceGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${model.format} • ${model.quantization} • ${model.formattedSize}",
                                color = DarkTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Status Actions
                        when (model.status) {
                            ModelDownloadStatus.NOT_DOWNLOADED -> {
                                Button(
                                    onClick = { onDownload(model.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("download_model_${model.id}")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download", fontSize = 11.sp)
                                }
                            }
                            ModelDownloadStatus.DOWNLOADING -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${model.downloadProgressPercent}%",
                                        color = StudioAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onCancel(model.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = StudioRose, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            ModelDownloadStatus.DOWNLOADED, ModelDownloadStatus.LOADED -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isActive) {
                                        Button(
                                            onClick = { onSetActive(model.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283D)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Set Active", fontSize = 11.sp, color = DarkTextPrimary)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(
                                        onClick = { onDelete(model.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (model.status == ModelDownloadStatus.DOWNLOADING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (model.downloadProgressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = StudioIndigo,
                            trackColor = Color(0xFF26334D)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = model.description,
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AiSettingsTab(
    systemPrompt: String,
    temperature: Float,
    onSystemPromptChange: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = Color(0xFF141C2E),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "System Prompt",
                    color = DarkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = onSystemPromptChange,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioIndigo,
                        unfocusedBorderColor = Color(0xFF26334D),
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Surface(
            color = Color(0xFF141C2E),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Temperature (Creativity)",
                        color = DarkTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.2f", temperature),
                        color = StudioIndigo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0.1f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = StudioIndigo,
                        activeTrackColor = StudioIndigo
                    )
                )
            }
        }
    }
}
