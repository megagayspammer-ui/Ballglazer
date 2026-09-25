package com.example.ui.elixir

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elixir.CardPlayEvent
import com.example.elixir.ElixirEngine
import com.example.elixir.ElixirMultiplier
import com.example.elixir.ElixirTrackerState
import com.example.model.ClashCard
import com.example.model.ClashCardDatabase
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.NamedDeviceGreen
import com.example.ui.theme.RadarNeonCyan
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioRose
import kotlin.math.roundToInt

val ElixirMagenta = Color(0xFFD90429)
val ElixirPurple = Color(0xFF9D0208)
val ElixirGlow = Color(0xFFFF0054)

@Composable
fun ElixirTrackerScreen(
    elixirEngine: ElixirEngine,
    trackerState: ElixirTrackerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCostFilter by remember { mutableStateOf<Int?>(null) }
    var showFloatingHudPreview by remember { mutableStateOf(false) }

    val hasOverlayPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        // Top Elixir Status Board
        ElixirDashboardHeader(
            state = trackerState,
            onToggleMatch = { elixirEngine.pauseOrResumeMatch() },
            onResetMatch = { elixirEngine.resetMatch() },
            onSetMultiplier = { elixirEngine.setMultiplier(it) },
            onAdjustOpponent = { delta -> elixirEngine.adjustOpponentElixir(delta) }
        )

        // Sub Tabs: [Cards Grid, Auto Analyzer & Overlay, Play History, Encyclopedia]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioDarkSurface,
            contentColor = ElixirGlow,
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Deploy Cards", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_deploy_cards")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Auto / Overlay", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_auto_overlay")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("History (${trackerState.cardHistory.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_history")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Database", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_database")
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> {
                    DeployCardsTab(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        costFilter = selectedCostFilter,
                        onCostFilterChange = { selectedCostFilter = it },
                        onCardClicked = { card ->
                            elixirEngine.playOpponentCard(card, source = "Manual Tap")
                        }
                    )
                }
                1 -> {
                    AutoOverlayTab(
                        state = trackerState,
                        hasOverlayPermission = hasOverlayPermission,
                        onToggleAuto = { elixirEngine.toggleAutoDetection(it) },
                        onRequestOverlayPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        showFloatingPreview = showFloatingHudPreview,
                        onToggleFloatingPreview = { showFloatingHudPreview = it }
                    )
                }
                2 -> {
                    PlayHistoryTab(history = trackerState.cardHistory)
                }
                3 -> {
                    CardDatabaseTab()
                }
            }
        }

        // Floating HUD Simulation overlay box if active
        if (showFloatingHudPreview) {
            FloatingHudPreviewOverlay(
                state = trackerState,
                onClose = { showFloatingHudPreview = false }
            )
        }
    }
}

@Composable
private fun ElixirDashboardHeader(
    state: ElixirTrackerState,
    onToggleMatch: () -> Unit,
    onResetMatch: () -> Unit,
    onSetMultiplier: (ElixirMultiplier) -> Unit,
    onAdjustOpponent: (Float) -> Unit
) {
    Surface(
        color = Color(0xFF131722),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Timer & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val minutes = state.matchTimeSeconds / 60
                    val seconds = state.matchTimeSeconds % 60
                    val timeString = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

                    Text(
                        text = timeString,
                        color = DarkTextPrimary,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = when (state.multiplier) {
                            ElixirMultiplier.ONE_X -> Color(0xFF1F2937)
                            ElixirMultiplier.TWO_X -> StudioAmber.copy(alpha = 0.25f)
                            ElixirMultiplier.THREE_X -> ElixirGlow.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = state.multiplier.label,
                            color = when (state.multiplier) {
                                ElixirMultiplier.ONE_X -> DarkTextSecondary
                                ElixirMultiplier.TWO_X -> StudioAmber
                                ElixirMultiplier.THREE_X -> ElixirGlow
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (state.isOpponentLeaking) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LEAKING!",
                            color = StudioRose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleMatch,
                        modifier = Modifier.size(34.dp).testTag("elixir_toggle_match")
                    ) {
                        Icon(
                            imageVector = if (state.isMatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Pause/Play",
                            tint = if (state.isMatchRunning) StudioAmber else NamedDeviceGreen
                        )
                    }

                    IconButton(
                        onClick = onResetMatch,
                        modifier = Modifier.size(34.dp).testTag("elixir_reset_match")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Match",
                            tint = DarkTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Giant Opponent Elixir Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "OPPONENT ELIXIR",
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", state.opponentElixir),
                            color = ElixirGlow,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " / 10",
                            color = DarkTextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Advantage Pill
                val adv = state.elixirAdvantage
                Surface(
                    color = if (adv >= 0) NamedDeviceGreen.copy(alpha = 0.2f) else StudioRose.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (adv >= 0) "+$adv Advantage" else "$adv Deficit",
                            color = if (adv >= 0) NamedDeviceGreen else StudioRose,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 10-Segment Elixir Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFF241426)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val fullBars = state.opponentElixir.toInt()
                val partial = state.opponentElixir - fullBars

                for (barIndex in 0 until 10) {
                    val fillFraction = when {
                        barIndex < fullBars -> 1f
                        barIndex == fullBars -> partial
                        else -> 0f
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp)
                            .background(Color(0xFF1A1322))
                    ) {
                        if (fillFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fillFraction)
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(ElixirPurple, ElixirGlow)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multiplier Switchers & Manual Quick Adjusters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multipliers
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ElixirMultiplier.values().forEach { mult ->
                        Surface(
                            color = if (state.multiplier == mult) ElixirGlow.copy(alpha = 0.25f) else Color(0xFF1E2433),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetMultiplier(mult) }
                        ) {
                            Text(
                                text = mult.label.take(2),
                                color = if (state.multiplier == mult) ElixirGlow else DarkTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Quick Elixir nudge buttons (-1, +1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFF1F293D),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onAdjustOpponent(-1f) }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("-1 Elixir", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        color = Color(0xFF1F293D),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onAdjustOpponent(1f) }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("+1 Elixir", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeployCardsTab(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    costFilter: Int?,
    onCostFilterChange: (Int?) -> Unit,
    onCardClicked: (ClashCard) -> Unit
) {
    val filteredCards = remember(searchQuery, costFilter) {
        ClashCardDatabase.allCards.filter { card ->
            val matchesCost = costFilter == null || card.cost == costFilter
            val matchesSearch = searchQuery.isBlank() || card.name.contains(searchQuery, ignoreCase = true)
            matchesCost && matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp)) {
        // Search & Cost filter pills
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = costFilter == null,
                onClick = { onCostFilterChange(null) },
                label = { Text("All Costs", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElixirGlow.copy(alpha = 0.25f),
                    selectedLabelColor = ElixirGlow,
                    containerColor = Color(0xFF192238),
                    labelColor = DarkTextPrimary
                )
            )

            (1..9).forEach { cost ->
                FilterChip(
                    selected = costFilter == cost,
                    onClick = { onCostFilterChange(if (costFilter == cost) null else cost) },
                    label = { Text("${cost}💧", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElixirGlow.copy(alpha = 0.25f),
                        selectedLabelColor = ElixirGlow,
                        containerColor = Color(0xFF192238),
                        labelColor = DarkTextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cards Grid (Quick Tap to Deduct Elixir)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().testTag("elixir_cards_grid")
        ) {
            items(filteredCards, key = { it.id }) { card ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(card.rarity.colorHex).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onCardClicked(card) }
                        .testTag("card_button_${card.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${card.cost}💧",
                                color = ElixirGlow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = card.rarity.label.take(1),
                                color = Color(card.rarity.colorHex),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = card.emoji,
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = card.name,
                            color = DarkTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Text(
                            text = "-${card.cost} Elixir",
                            color = StudioRose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoOverlayTab(
    state: ElixirTrackerState,
    hasOverlayPermission: Boolean,
    onToggleAuto: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    showFloatingPreview: Boolean,
    onToggleFloatingPreview: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Floating HUD Overlay Card
        Surface(
            color = Color(0xFF141C2E),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF26334D), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = RadarNeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Floating HUD Overlay",
                        color = DarkTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Display a persistent floating elixir widget directly over Clash Royale so you can track cards without switching apps.",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!hasOverlayPermission) {
                    Button(
                        onClick = onRequestOverlayPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        modifier = Modifier.fillMaxWidth().testTag("grant_overlay_btn")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Draw Over Apps Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NamedDeviceGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Permission Granted", color = NamedDeviceGreen, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onToggleFloatingPreview(!showFloatingPreview) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showFloatingPreview) StudioRose else RadarNeonCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("toggle_hud_preview_btn")
                        ) {
                            Text(
                                text = if (showFloatingPreview) "Hide HUD" else "Show Floating HUD",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Auto Card & 2X/3X Screen Detection Card
        Surface(
            color = Color(0xFF141C2E),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF26334D), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = ElixirGlow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto Screen Analyzer",
                                color = DarkTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OCR & Optical Card Recognition",
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = state.isAutoDetectionEnabled,
                        onCheckedChange = { onToggleAuto(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ElixirGlow
                        ),
                        modifier = Modifier.testTag("auto_detect_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Continuously watches gameplay via MediaProjection to detect deployed enemy cards (e.g., -7 elixir for P.E.K.K.A) and recognizes when the 'Double Elixir' or 'Triple Elixir' banners flash on screen to adjust regeneration rates.",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF101725),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.isAutoDetectionEnabled) NamedDeviceGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.autoDetectionStatus,
                            color = if (state.isAutoDetectionEnabled) DarkTextPrimary else DarkTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayHistoryTab(history: List<CardPlayEvent>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No cards played in this match yet", color = DarkTextSecondary, fontSize = 13.sp)
                Text("Tap any card on the Deploy tab or enable Auto Detection", color = DarkTextSecondary.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { event ->
                Surface(
                    color = Color(0xFF141C2E),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = event.card.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.card.name,
                                color = DarkTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Source: ${event.source}",
                                color = DarkTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${event.card.cost} 💧",
                                color = StudioRose,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f → %.1f", event.elixirBefore, event.elixirAfter),
                                color = DarkTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDatabaseTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ClashCardDatabase.allCards, key = { it.id }) { card ->
            Surface(
                color = Color(0xFF141C2E),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = card.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = card.name,
                                color = DarkTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = card.rarity.label,
                                color = Color(card.rarity.colorHex),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = card.description,
                            color = DarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${card.cost} Elixir",
                        color = ElixirGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingHudPreviewOverlay(
    state: ElixirTrackerState,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            color = Color(0xEE121826),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(top = 40.dp)
                .width(280.dp)
                .border(1.5.dp, ElixirGlow, RoundedCornerShape(16.dp))
                .clickable { /* absorb clicks */ }
                .testTag("floating_hud_widget")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HUD: OPPONENT ELIXIR",
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.multiplier.label,
                        color = StudioAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", state.opponentElixir),
                        color = ElixirGlow,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    val adv = state.elixirAdvantage
                    Text(
                        text = if (adv >= 0) "+$adv Lead" else "$adv",
                        color = if (adv >= 0) NamedDeviceGreen else StudioRose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (state.opponentElixir / 10f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = ElixirGlow,
                    trackColor = Color(0xFF261424)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to dismiss preview",
                    color = DarkTextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
