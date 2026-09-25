package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ai.LocalAiScreen
import com.example.ui.elixir.ElixirGlow
import com.example.ui.elixir.ElixirTrackerScreen
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.RadarNeonCyan
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioIndigo
import com.example.viewmodel.RadarViewModel

enum class SuperAppTab(val label: String) {
    RADAR("RF Radar"),
    ELIXIR("Elixir Master"),
    LOCAL_AI("Local AI")
}

@Composable
fun SuperAppScreen(
    viewModel: RadarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val elixirState by viewModel.elixirState.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = StudioDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("superapp_nav_bar")
            ) {
                // Tab 0: Radar
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar"
                        )
                    },
                    label = {
                        Text(
                            text = "RF Radar",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RadarNeonCyan,
                        selectedTextColor = RadarNeonCyan,
                        indicatorColor = RadarNeonCyan.copy(alpha = 0.2f),
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_radar")
                )

                // Tab 1: Elixir Master
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Elixir Master"
                        )
                    },
                    label = {
                        Text(
                            text = "Elixir Tracker",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElixirGlow,
                        selectedTextColor = ElixirGlow,
                        indicatorColor = ElixirGlow.copy(alpha = 0.2f),
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_elixir")
                )

                // Tab 2: Local AI
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Local AI"
                        )
                    },
                    label = {
                        Text(
                            text = "Local AI",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StudioIndigo,
                        selectedTextColor = StudioIndigo,
                        indicatorColor = StudioIndigo.copy(alpha = 0.2f),
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_ai")
                )
            }
        },
        containerColor = StudioDarkBg,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> RadarScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                1 -> ElixirTrackerScreen(
                    elixirEngine = viewModel.elixirEngine,
                    trackerState = elixirState,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> LocalAiScreen(
                    aiManager = viewModel.aiManager,
                    state = aiState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
