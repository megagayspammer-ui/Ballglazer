package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ElixirApplication
import com.example.elixir.CardCycleStatus
import com.example.elixir.CardPlayEvent
import com.example.elixir.ElixirEngine
import com.example.elixir.ElixirMultiplier
import com.example.elixir.ElixirTrackerState
import com.example.model.CardRole
import com.example.model.ClashCard
import com.example.model.ClashCardDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class AppNavTab(val label: String) {
    LIVE_MATCH("Live Match"),
    DECK_CYCLE("Deck & Cycle"),
    OVERLAY("Overlay / Auto"),
    ALMANAC("Card Guide"),
    SETTINGS("Settings")
}

class ElixirViewModel(application: Application) : AndroidViewModel(application) {

    val engine: ElixirEngine = (application as? ElixirApplication)?.engine ?: ElixirEngine(viewModelScope, application)
    val state: StateFlow<ElixirTrackerState> = engine.state

    private val _currentTab = MutableStateFlow(AppNavTab.LIVE_MATCH)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCostFilter = MutableStateFlow<Int?>(null)
    val selectedCostFilter: StateFlow<Int?> = _selectedCostFilter.asStateFlow()

    private val _selectedRoleFilter = MutableStateFlow(CardRole.ALL)
    val selectedRoleFilter: StateFlow<CardRole> = _selectedRoleFilter.asStateFlow()

    private val _showFloatingHudPreview = MutableStateFlow(false)
    val showFloatingHudPreview: StateFlow<Boolean> = _showFloatingHudPreview.asStateFlow()

    val filteredCards: StateFlow<List<ClashCard>> = combine(
        _searchQuery,
        _selectedCostFilter,
        _selectedRoleFilter
    ) { query, cost, role ->
        ClashCardDatabase.searchCards(query = query, role = role, costFilter = cost)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClashCardDatabase.allCards)

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCostFilter(cost: Int?) {
        _selectedCostFilter.value = cost
    }

    fun setRoleFilter(role: CardRole) {
        _selectedRoleFilter.value = role
    }

    fun toggleFloatingHudPreview(show: Boolean) {
        _showFloatingHudPreview.value = show
    }

    fun setScreenCaptureActive(active: Boolean) {
        engine.setMediaProjectionActive(active)
    }

    // Engine forwarding actions
    fun startMatch() = engine.startMatch()
    fun pauseOrResume() = engine.pauseOrResumeMatch()
    fun resetMatch() = engine.resetMatch()
    fun setMultiplier(multiplier: ElixirMultiplier) = engine.setMultiplier(multiplier)
    fun playOpponentCard(card: ClashCard) = engine.playOpponentCard(card, source = "Manual Tap")
    fun playUserCard(card: ClashCard) = engine.playUserCard(card)
    fun undoLastCardPlay() = engine.undoLastCardPlay()
    fun adjustOpponent(delta: Float) = engine.adjustOpponentElixir(delta)
    fun adjustUser(delta: Float) = engine.adjustUserElixir(delta)
    fun setOverlayPermission(granted: Boolean) = engine.setOverlayPermissionGranted(granted)
    fun setHaptics(enabled: Boolean) = engine.setHapticEnabled(enabled)
    fun setAutoStart(enabled: Boolean) = engine.setAutoStartOnFirstPlay(enabled)
}
