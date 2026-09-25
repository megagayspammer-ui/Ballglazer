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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.ViewCarousel
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elixir.CardCycleStatus
import com.example.elixir.CardPlayEvent
import com.example.elixir.ElixirMultiplier
import com.example.elixir.ElixirTrackerState
import com.example.model.CardRole
import com.example.model.ClashCard
import com.example.model.ClashCardDatabase
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DeficitRed
import com.example.ui.theme.ElixirBright
import com.example.ui.theme.ElixirDeep
import com.example.ui.theme.ElixirGaugeTrack
import com.example.ui.theme.ElixirMagenta
import com.example.ui.theme.EsportsBorder
import com.example.ui.theme.EsportsCard
import com.example.ui.theme.EsportsSlate
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PositiveGreen
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.ElixirViewModel
import kotlin.math.roundToInt

@Composable
fun ElixirMainScreen(
    viewModel: ElixirViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val filteredCards by viewModel.filteredCards.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCost by viewModel.selectedCostFilter.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedRoleFilter.collectAsStateWithLifecycle()
    val showFloatingHud by viewModel.showFloatingHudPreview.collectAsStateWithLifecycle()

    val hasOverlayPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    Scaffold(
        containerColor = EsportsSlate,
        bottomBar = {
            EsportsNavigationBar(
                currentTab = currentTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Persistent Match Bar across match tabs
                MatchDashboardHeader(
                    state = state,
                    onToggleMatch = { viewModel.pauseOrResume() },
                    onResetMatch = { viewModel.resetMatch() },
                    onUndoPlay = { viewModel.undoLastCardPlay() },
                    onSetMultiplier = { viewModel.setMultiplier(it) },
                    onAdjustOpponent = { viewModel.adjustOpponent(it) },
                    onAdjustUser = { viewModel.adjustUser(it) }
                )

                // Tab Content View
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (currentTab) {
                        AppNavTab.LIVE_MATCH -> {
                            LiveMatchTab(
                                state = state,
                                cards = filteredCards,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                selectedCost = selectedCost,
                                onSelectCost = { viewModel.setCostFilter(it) },
                                selectedRole = selectedRole,
                                onSelectRole = { viewModel.setRoleFilter(it) },
                                onCardClick = { viewModel.playOpponentCard(it) },
                                onUserCardClick = { viewModel.playUserCard(it) },
                                onUndoPlay = { viewModel.undoLastCardPlay() }
                            )
                        }
                        AppNavTab.DECK_CYCLE -> {
                            DeckCycleTab(
                                state = state,
                                onCardClick = { viewModel.playOpponentCard(it) },
                                onUndoPlay = { viewModel.undoLastCardPlay() }
                            )
                        }
                        AppNavTab.OVERLAY -> {
                            OverlayAutoTab(
                                state = state,
                                hasOverlayPermission = hasOverlayPermission,
                                onRequestPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                },
                                onToggleAuto = { viewModel.toggleAutoDetection(it) },
                                showFloatingHud = showFloatingHud,
                                onToggleFloatingHud = { viewModel.toggleFloatingHudPreview(it) }
                            )
                        }
                        AppNavTab.ALMANAC -> {
                            AlmanacTab(
                                cards = filteredCards,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                selectedRole = selectedRole,
                                onSelectRole = { viewModel.setRoleFilter(it) }
                            )
                        }
                        AppNavTab.SETTINGS -> {
                            SettingsTab(
                                state = state,
                                onToggleHaptics = { viewModel.setHaptics(it) },
                                onToggleAutoStart = { viewModel.setAutoStart(it) },
                                onResetMatch = { viewModel.resetMatch() }
                            )
                        }
                    }
                }
            }

            // Draggable / Dismissible Floating HUD simulator preview
            if (showFloatingHud) {
                FloatingHudOverlayWidget(
                    state = state,
                    onDismiss = { viewModel.toggleFloatingHudPreview(false) }
                )
            }
        }
    }
}

@Composable
private fun MatchDashboardHeader(
    state: ElixirTrackerState,
    onToggleMatch: () -> Unit,
    onResetMatch: () -> Unit,
    onUndoPlay: () -> Unit,
    onSetMultiplier: (ElixirMultiplier) -> Unit,
    onAdjustOpponent: (Float) -> Unit,
    onAdjustUser: (Float) -> Unit
) {
    Surface(
        color = EsportsSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Row 1: Clock, Multiplier, Status, Quick Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.formattedTime,
                        color = DarkTextPrimary,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("match_clock_text")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = when (state.multiplier) {
                            ElixirMultiplier.ONE_X -> Color(0xFF1E293B)
                            ElixirMultiplier.TWO_X -> GoldAccent.copy(alpha = 0.2f)
                            ElixirMultiplier.THREE_X -> ElixirBright.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(
                            1.dp,
                            when (state.multiplier) {
                                ElixirMultiplier.ONE_X -> Color(0xFF334155)
                                ElixirMultiplier.TWO_X -> GoldAccent.copy(alpha = 0.6f)
                                ElixirMultiplier.THREE_X -> ElixirBright.copy(alpha = 0.6f)
                            },
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        Text(
                            text = state.multiplier.label,
                            color = when (state.multiplier) {
                                ElixirMultiplier.ONE_X -> DarkTextSecondary
                                ElixirMultiplier.TWO_X -> GoldAccent
                                ElixirMultiplier.THREE_X -> ElixirBright
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (state.isOpponentLeaking) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = DeficitRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LEAKING!",
                                color = DeficitRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (state.cardHistory.isNotEmpty()) {
                        IconButton(
                            onClick = onUndoPlay,
                            modifier = Modifier.size(32.dp).testTag("header_undo_play")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo Last Card Play",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleMatch,
                        modifier = Modifier.size(32.dp).testTag("header_toggle_match")
                    ) {
                        Icon(
                            imageVector = if (state.isMatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Start/Pause Match",
                            tint = if (state.isMatchRunning) GoldAccent else PositiveGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onResetMatch,
                        modifier = Modifier.size(32.dp).testTag("header_reset_match")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Match",
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Anti-Multi-Count Notice / Action Banner
            state.lastNoticeMessage?.let { notice ->
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF141D30),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = notice,
                            color = DarkTextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Opponent Elixir Readout & Elixir Advantage Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "OPPONENT ELIXIR",
                        color = DarkTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", state.opponentElixir),
                            color = ElixirBright,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("opponent_elixir_display")
                        )
                        Text(
                            text = " / 10",
                            color = DarkTextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
                        )
                    }
                }

                // Advantage / Deficit Indicator
                val advantage = state.elixirAdvantage
                Surface(
                    color = when {
                        advantage > 0 -> PositiveGreen.copy(alpha = 0.15f)
                        advantage < 0 -> DeficitRed.copy(alpha = 0.15f)
                        else -> Color(0xFF1E293B)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(
                            1.dp,
                            when {
                                advantage > 0 -> PositiveGreen.copy(alpha = 0.4f)
                                advantage < 0 -> DeficitRed.copy(alpha = 0.4f)
                                else -> Color(0xFF334155)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("elixir_advantage_pill")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = when {
                                advantage > 0 -> "+$advantage Advantage"
                                advantage < 0 -> "$advantage Deficit"
                                else -> "Even Trade (0)"
                            },
                            color = when {
                                advantage > 0 -> PositiveGreen
                                advantage < 0 -> DeficitRed
                                else -> DarkTextSecondary
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "My Elixir: ${String.format(java.util.Locale.US, "%.1f", state.userElixir)}",
                            color = DarkTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 10-Segment Tournament Fluid Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElixirGaugeTrack)
                    .border(1.dp, Color(0xFF3B1E45), RoundedCornerShape(6.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val fullBars = state.opponentElixir.toInt()
                val partialFraction = (state.opponentElixir - fullBars).coerceIn(0f, 1f)

                for (barIndex in 0 until 10) {
                    val fill = when {
                        barIndex < fullBars -> 1f
                        barIndex == fullBars -> partialFraction
                        else -> 0f
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp)
                            .background(Color(0xFF190F24))
                    ) {
                        if (fill > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fill)
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(ElixirDeep, ElixirBright)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Nudge Keys & Multiplier Direct Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multiplier Selector Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ElixirMultiplier.values().forEach { mult ->
                        Surface(
                            color = if (state.multiplier == mult) ElixirBright.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable { onSetMultiplier(mult) }
                                .border(
                                    1.dp,
                                    if (state.multiplier == mult) ElixirBright else Color(0xFF334155),
                                    RoundedCornerShape(6.dp)
                                )
                                .testTag("mult_btn_${mult.name}")
                        ) {
                            Text(
                                text = mult.label.take(2),
                                color = if (state.multiplier == mult) ElixirBright else DarkTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Opponent Elixir Quick Adjustment & Undo
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onAdjustOpponent(-1f) }
                            .testTag("nudge_minus_1")
                    ) {
                        Text(
                            text = "-1",
                            color = DarkTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onAdjustOpponent(1f) }
                            .testTag("nudge_plus_1")
                    ) {
                        Text(
                            text = "+1",
                            color = DarkTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onAdjustUser(-1f) }
                            .testTag("nudge_my_minus_1")
                    ) {
                        Text(
                            text = "My -1💧",
                            color = DarkTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveMatchTab(
    state: ElixirTrackerState,
    cards: List<ClashCard>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCost: Int?,
    onSelectCost: (Int?) -> Unit,
    selectedRole: CardRole,
    onSelectRole: (CardRole) -> Unit,
    onCardClick: (ClashCard) -> Unit,
    onUserCardClick: (ClashCard) -> Unit,
    onUndoPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Discovered Opponent Cards Cycle Ribbon
        if (state.opponentDeck.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OPPONENT CYCLE (${state.opponentDeck.size}/8 REVEALED)",
                    color = DarkTextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                if (state.cardHistory.isNotEmpty()) {
                    Text(
                        text = "Undo Last",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onUndoPlay() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.opponentCycleStatuses.forEach { cycle ->
                    Surface(
                        color = if (cycle.inHand) PositiveGreen.copy(alpha = 0.15f) else EsportsCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (cycle.inHand) PositiveGreen.copy(alpha = 0.6f) else EsportsBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onCardClick(cycle.card) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cycle.card.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = cycle.card.name,
                                    color = DarkTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = cycle.displayStatus,
                                    color = if (cycle.inHand) PositiveGreen else GoldAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Search Bar & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Filter cards (e.g. Pekka, 4, Spell)", fontSize = 11.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = DarkTextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElixirBright,
                    unfocusedBorderColor = EsportsBorder,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("card_search_input")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Cost Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedCost == null,
                onClick = { onSelectCost(null) },
                label = { Text("All", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElixirBright.copy(alpha = 0.25f),
                    selectedLabelColor = ElixirBright,
                    containerColor = EsportsCard,
                    labelColor = DarkTextSecondary
                )
            )

            (1..9).forEach { cost ->
                FilterChip(
                    selected = selectedCost == cost,
                    onClick = { onSelectCost(if (selectedCost == cost) null else cost) },
                    label = { Text("${cost}💧", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElixirBright.copy(alpha = 0.25f),
                        selectedLabelColor = ElixirBright,
                        containerColor = EsportsCard,
                        labelColor = DarkTextSecondary
                    ),
                    modifier = Modifier.testTag("filter_cost_$cost")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Card Selection Grid with Anti-Multi-Count indicator
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("elixir_deployment_grid")
        ) {
            items(cards, key = { it.id }) { card ->
                val cycleInfo = state.opponentCycleStatuses.find { it.card.id == card.id }
                val isCycling = cycleInfo != null && !cycleInfo.inHand

                Card(
                    colors = CardDefaults.cardColors(containerColor = EsportsCard),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCycling) GoldAccent.copy(alpha = 0.5f) else Color(card.rarity.colorHex).copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onCardClick(card) }
                        .testTag("deploy_card_${card.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${card.cost}💧",
                                color = ElixirBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            if (isCycling) {
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = cycleInfo.displayStatus,
                                        color = GoldAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = card.rarity.label.take(1),
                                    color = Color(card.rarity.colorHex),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = card.emoji,
                            fontSize = 26.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = card.name,
                            color = DarkTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Text(
                            text = "-${card.cost} Elixir",
                            color = DeficitRed,
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
private fun DeckCycleTab(
    state: ElixirTrackerState,
    onCardClick: (ClashCard) -> Unit,
    onUndoPlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OPPONENT DECK & 4-CARD ROTATION",
                    color = DarkTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    color = PositiveGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Anti-Multi-Count: ACTIVE",
                        color = PositiveGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Discovered Deck Overview Card
        item {
            Surface(
                color = EsportsCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EsportsBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Discovered Cards (${state.opponentDeck.size}/8)",
                            color = DarkTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val avgCost = if (state.opponentDeck.isNotEmpty()) {
                            state.opponentDeck.map { it.cost }.average()
                        } else 0.0

                        Text(
                            text = String.format(java.util.Locale.US, "Avg: %.1f💧", avgCost),
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.opponentDeck.isEmpty()) {
                        Text(
                            text = "No opponent cards seen yet. As cards are deployed, the 8-card profile will build automatically without duplicates.",
                            color = DarkTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((state.opponentDeck.size + 3) / 4 * 72).coerceAtLeast(72).dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.opponentCycleStatuses) { cycle ->
                                Surface(
                                    color = if (cycle.inHand) PositiveGreen.copy(alpha = 0.15f) else Color(0xFF131A2A),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(
                                        1.dp,
                                        if (cycle.inHand) PositiveGreen.copy(alpha = 0.5f) else EsportsBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = cycle.card.emoji, fontSize = 18.sp)
                                        Text(
                                            text = cycle.card.name,
                                            color = DarkTextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = cycle.displayStatus,
                                            color = if (cycle.inHand) PositiveGreen else DarkTextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Play Timeline Section with Undo Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATCH TIMELINE (${state.cardHistory.size} PLAYS)",
                    color = DarkTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                if (state.cardHistory.isNotEmpty()) {
                    Button(
                        onClick = onUndoPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("timeline_undo_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo Last Play", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.cardHistory.isEmpty()) {
            item {
                Surface(
                    color = EsportsCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No card plays recorded in this match yet", color = DarkTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(state.cardHistory, key = { it.id }) { event ->
                Surface(
                    color = EsportsCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = event.card.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = event.card.name,
                                    color = DarkTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (event.isOpponent) "Opponent" else "You",
                                    color = if (event.isOpponent) DeficitRed else PositiveGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Source: ${event.source} • Clock: ${event.matchSecond / 60}:${String.format(java.util.Locale.US, "%02d", event.matchSecond % 60)}",
                                color = DarkTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${event.card.cost} 💧",
                                color = if (event.isOpponent) DeficitRed else PositiveGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f → %.1f", event.elixirBefore, event.elixirAfter),
                                color = DarkTextMuted,
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
private fun OverlayAutoTab(
    state: ElixirTrackerState,
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit,
    onToggleAuto: (Boolean) -> Unit,
    showFloatingHud: Boolean,
    onToggleFloatingHud: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Floating HUD Overlay Config
        Surface(
            color = EsportsCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EsportsBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Floating In-Game HUD",
                            color = DarkTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Draws directly over Clash Royale gameplay",
                            color = DarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "A compact, lightweight overlay that renders the opponent's elixir count and current 1X/2X/3X rate on top of your arena match.",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!hasOverlayPermission) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_grant_overlay")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Overlay Permission", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PositiveGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("System Permission Active", color = PositiveGreen, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onToggleFloatingHud(!showFloatingHud) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showFloatingHud) DeficitRed else CyanAccent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_toggle_hud_preview")
                        ) {
                            Text(
                                text = if (showFloatingHud) "Hide HUD" else "Test Floating HUD",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Screen Capture & OCR Card Detection
        Surface(
            color = EsportsCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EsportsBorder, RoundedCornerShape(12.dp))
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
                            tint = ElixirBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto Screen Analyzer",
                                color = DarkTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Card Detection with Multi-Count Cooldown",
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
                            checkedTrackColor = ElixirBright
                        ),
                        modifier = Modifier.testTag("switch_auto_detection")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Continuously watches gameplay via MediaProjection to detect deployed enemy cards and recognizes 2X/3X banners. Includes 16-second on-field presence cooldown so units walking across lanes are never multi-counted.",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF0F1522),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (state.isAutoDetectionEnabled) PositiveGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.autoDetectionStatus,
                            color = if (state.isAutoDetectionEnabled) DarkTextPrimary else DarkTextMuted,
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
private fun AlmanacTab(
    cards: List<ClashCard>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedRole: CardRole,
    onSelectRole: (CardRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "CARD ALMANAC & POSITIVE TRADE GUIDE",
            color = DarkTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CardRole.values().forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { onSelectRole(role) },
                    label = { Text(role.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElixirBright.copy(alpha = 0.25f),
                        selectedLabelColor = ElixirBright,
                        containerColor = EsportsCard,
                        labelColor = DarkTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cards, key = { it.id }) { card ->
                Surface(
                    color = EsportsCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(card.rarity.colorHex).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = card.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = card.name,
                                            color = DarkTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (card.isWinCondition) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = GoldAccent.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "WIN CON",
                                                    color = GoldAccent,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${card.rarity.label} • ${card.role.label}",
                                        color = DarkTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = "${card.cost}💧",
                                color = ElixirBright,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = card.description,
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        if (card.positiveTradeTip.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = PositiveGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Trade Tip: ${card.positiveTradeTip}",
                                        color = PositiveGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: ElixirTrackerState,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onResetMatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "MATCH & APP SETTINGS",
            color = DarkTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        // Multi-Counting Safeguards Card
        Surface(
            color = EsportsCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anti-Multi-Count Protection", color = DarkTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Rapid Double-Tap Suppression: Ignores duplicate taps on the same card within 650ms.\n• Rotation Cycle Guard: Highlights cards currently in the 4-card rotation.\n• Vision Presence Cooldown: 16-second cooldown on optical card detection prevents troops walking down lanes from being multi-counted.\n• Instant Undo: Refunds exact elixir on misclicks.",
                    color = DarkTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Surface(
            color = EsportsCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Haptic Feedback", color = DarkTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Vibrates on card taps and 10-elixir cap", color = DarkTextSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = state.hapticEnabled,
                        onCheckedChange = onToggleHaptics,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ElixirBright
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Start Timer", color = DarkTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Starts match clock when first card is played", color = DarkTextSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = state.autoStartOnFirstPlay,
                        onCheckedChange = onToggleAutoStart,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ElixirBright
                        )
                    )
                }
            }
        }

        // About the Elixir Engine Math
        Surface(
            color = EsportsCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Authentic Clash Royale Timing Math",
                    color = DarkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• 1X Elixir (0:00 - 2:00): 1 bar per 2.80s (0.357 el/s)\n• 2X Elixir (2:00 - 4:00): 1 bar per 1.40s (0.714 el/s)\n• 3X Elixir (4:00+ Overtime): 1 bar per 0.93s (1.075 el/s)\n• 4-Card Rotation Rule: After playing a card, exactly 4 other distinct cards must be deployed before that card returns to the active 4-card hand.",
                    color = DarkTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun EsportsNavigationBar(
    currentTab: AppNavTab,
    onSelectTab: (AppNavTab) -> Unit
) {
    NavigationBar(
        containerColor = EsportsSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("app_nav_bar")
    ) {
        AppNavTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            AppNavTab.LIVE_MATCH -> Icons.Default.Bolt
                            AppNavTab.DECK_CYCLE -> Icons.Default.ViewCarousel
                            AppNavTab.OVERLAY -> Icons.Default.Layers
                            AppNavTab.ALMANAC -> Icons.Default.MenuBook
                            AppNavTab.SETTINGS -> Icons.Default.Settings
                        },
                        contentDescription = tab.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = when (tab) {
                            AppNavTab.LIVE_MATCH -> "Match"
                            AppNavTab.DECK_CYCLE -> "Cycle"
                            AppNavTab.OVERLAY -> "Overlay"
                            AppNavTab.ALMANAC -> "Almanac"
                            AppNavTab.SETTINGS -> "Settings"
                        },
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElixirBright,
                    selectedTextColor = ElixirBright,
                    indicatorColor = ElixirBright.copy(alpha = 0.2f),
                    unselectedIconColor = DarkTextSecondary,
                    unselectedTextColor = DarkTextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name}")
            )
        }
    }
}

@Composable
private fun FloatingHudOverlayWidget(
    state: ElixirTrackerState,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            color = Color(0xF00B0F19),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .padding(top = 40.dp)
                .width(280.dp)
                .border(1.5.dp, ElixirBright, RoundedCornerShape(14.dp))
                .clickable { /* consume clicks */ }
                .testTag("floating_hud_preview_box")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HUD OVERLAY",
                        color = DarkTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        color = when (state.multiplier) {
                            ElixirMultiplier.ONE_X -> Color(0xFF1E293B)
                            ElixirMultiplier.TWO_X -> GoldAccent.copy(alpha = 0.25f)
                            ElixirMultiplier.THREE_X -> ElixirBright.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = state.multiplier.label,
                            color = when (state.multiplier) {
                                ElixirMultiplier.ONE_X -> DarkTextSecondary
                                ElixirMultiplier.TWO_X -> GoldAccent
                                ElixirMultiplier.THREE_X -> ElixirBright
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", state.opponentElixir),
                        color = ElixirBright,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    val adv = state.elixirAdvantage
                    Text(
                        text = when {
                            adv > 0 -> "+$adv Lead"
                            adv < 0 -> "$adv"
                            else -> "Even"
                        },
                        color = when {
                            adv > 0 -> PositiveGreen
                            adv < 0 -> DeficitRed
                            else -> DarkTextSecondary
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (state.opponentElixir / 10f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElixirBright,
                    trackColor = ElixirGaugeTrack
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap backdrop to close preview",
                    color = DarkTextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
