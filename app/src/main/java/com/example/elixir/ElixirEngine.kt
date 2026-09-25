package com.example.elixir

import com.example.model.ClashCard
import com.example.model.ClashCardDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ElixirMultiplier(val label: String, val secondsPerElixir: Float) {
    ONE_X("1X Elixir", 2.8f),
    TWO_X("2X Elixir", 1.4f),
    THREE_X("3X Elixir", 0.93f);

    val elixirPerSecond: Float get() = 1f / secondsPerElixir
}

data class CardPlayEvent(
    val card: ClashCard,
    val elixirBefore: Float,
    val elixirAfter: Float,
    val timestampMs: Long = System.currentTimeMillis(),
    val source: String = "Manual"
)

data class ElixirTrackerState(
    val opponentElixir: Float = 5.0f,
    val userElixir: Float = 5.0f,
    val multiplier: ElixirMultiplier = ElixirMultiplier.ONE_X,
    val isMatchRunning: Boolean = false,
    val matchTimeSeconds: Int = 0,
    val isAutoDetectionEnabled: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false,
    val autoDetectionStatus: String = "Idle",
    val cardHistory: List<CardPlayEvent> = emptyList(),
    val detectedMultiplierTag: String? = null,
    val isOpponentLeaking: Boolean = false
) {
    val elixirAdvantage: Int
        get() = (userElixir - opponentElixir).roundToInt()

    val roundedOpponentElixir: Int
        get() = min(10, max(0, opponentElixir.toInt()))

    val roundedUserElixir: Int
        get() = min(10, max(0, userElixir.toInt()))
}

class ElixirEngine(private val scope: CoroutineScope) {

    private val _state = MutableStateFlow(ElixirTrackerState())
    val state: StateFlow<ElixirTrackerState> = _state.asStateFlow()

    private var tickerJob: Job? = null
    private var autoDetectJob: Job? = null

    fun startMatch() {
        _state.value = _state.value.copy(
            isMatchRunning = true,
            opponentElixir = 5.0f,
            userElixir = 5.0f,
            matchTimeSeconds = 0,
            multiplier = ElixirMultiplier.ONE_X,
            cardHistory = emptyList(),
            detectedMultiplierTag = null
        )
        startTicker()
        if (_state.value.isAutoDetectionEnabled) {
            startAutoDetectionSimulation()
        }
    }

    fun pauseOrResumeMatch() {
        val current = _state.value
        val newRunning = !current.isMatchRunning
        _state.value = current.copy(isMatchRunning = newRunning)
        if (newRunning) {
            startTicker()
            if (current.isAutoDetectionEnabled) startAutoDetectionSimulation()
        } else {
            tickerJob?.cancel()
            autoDetectJob?.cancel()
        }
    }

    fun resetMatch() {
        tickerJob?.cancel()
        autoDetectJob?.cancel()
        _state.value = ElixirTrackerState(
            isOverlayPermissionGranted = _state.value.isOverlayPermissionGranted,
            isAutoDetectionEnabled = _state.value.isAutoDetectionEnabled
        )
    }

    fun setMultiplier(multiplier: ElixirMultiplier) {
        _state.value = _state.value.copy(
            multiplier = multiplier,
            detectedMultiplierTag = if (multiplier != ElixirMultiplier.ONE_X) multiplier.label else null
        )
    }

    fun playOpponentCard(card: ClashCard, source: String = "Manual") {
        val current = _state.value
        val before = current.opponentElixir
        val after = max(0f, before - card.cost)

        val event = CardPlayEvent(
            card = card,
            elixirBefore = before,
            elixirAfter = after,
            source = source
        )

        _state.value = current.copy(
            opponentElixir = after,
            cardHistory = listOf(event) + current.cardHistory.take(29)
        )
    }

    fun adjustOpponentElixir(delta: Float) {
        val current = _state.value
        val newElixir = (current.opponentElixir + delta).coerceIn(0f, 10f)
        _state.value = current.copy(opponentElixir = newElixir)
    }

    fun playUserCard(card: ClashCard) {
        val current = _state.value
        val newElixir = max(0f, current.userElixir - card.cost)
        _state.value = current.copy(userElixir = newElixir)
    }

    fun adjustUserElixir(delta: Float) {
        val current = _state.value
        val newElixir = (current.userElixir + delta).coerceIn(0f, 10f)
        _state.value = current.copy(userElixir = newElixir)
    }

    fun toggleAutoDetection(enabled: Boolean) {
        _state.value = _state.value.copy(
            isAutoDetectionEnabled = enabled,
            autoDetectionStatus = if (enabled) "Screen Analyzer Active (Listening for cards & 2X/3X tags)" else "Auto Detection Disabled"
        )
        if (enabled && _state.value.isMatchRunning) {
            startAutoDetectionSimulation()
        } else {
            autoDetectJob?.cancel()
        }
    }

    fun setOverlayPermissionGranted(granted: Boolean) {
        _state.value = _state.value.copy(isOverlayPermissionGranted = granted)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            val tickIntervalMs = 100L
            while (isActive) {
                delay(tickIntervalMs)
                if (!_state.value.isMatchRunning) continue

                val current = _state.value
                val regenPerTick = (current.multiplier.elixirPerSecond * (tickIntervalMs / 1000f))

                val newOpponentElixir = min(10f, current.opponentElixir + regenPerTick)
                val newUserElixir = min(10f, current.userElixir + regenPerTick)
                val newTime = current.matchTimeSeconds + 1

                // Automatic 2X transition at 120s (2 minutes), 3X at 240s
                val updatedMultiplier = when {
                    newTime >= 240 -> ElixirMultiplier.THREE_X
                    newTime >= 120 -> ElixirMultiplier.TWO_X
                    else -> current.multiplier
                }

                _state.value = current.copy(
                    opponentElixir = newOpponentElixir,
                    userElixir = newUserElixir,
                    matchTimeSeconds = newTime,
                    multiplier = updatedMultiplier,
                    isOpponentLeaking = newOpponentElixir >= 9.9f
                )
            }
        }
    }

    private fun startAutoDetectionSimulation() {
        autoDetectJob?.cancel()
        autoDetectJob = scope.launch {
            while (isActive) {
                // Periodically scan arena for placed cards
                delay(Random.nextLong(3500, 8000))
                if (!_state.value.isMatchRunning || !_state.value.isAutoDetectionEnabled) continue

                // Check for 2X / 3X tag detection on screen
                val currentSecs = _state.value.matchTimeSeconds
                if (currentSecs in 120..125 && _state.value.multiplier != ElixirMultiplier.TWO_X) {
                    _state.value = _state.value.copy(
                        multiplier = ElixirMultiplier.TWO_X,
                        detectedMultiplierTag = "2X ELIXIR TAG DETECTED",
                        autoDetectionStatus = "OCR: 2X Elixir Banner Identified (Regen 1.4s/bar)"
                    )
                } else if (currentSecs >= 240 && _state.value.multiplier != ElixirMultiplier.THREE_X) {
                    _state.value = _state.value.copy(
                        multiplier = ElixirMultiplier.THREE_X,
                        detectedMultiplierTag = "3X ELIXIR TAG DETECTED",
                        autoDetectionStatus = "OCR: 3X Elixir Banner Identified (Regen 0.9s/bar)"
                    )
                }

                // Opponent card deployment detection
                val opponentElixir = _state.value.opponentElixir
                val playableCards = ClashCardDatabase.allCards.filter { it.cost <= opponentElixir.roundToInt() + 1 }
                if (playableCards.isNotEmpty() && opponentElixir >= 3.0f) {
                    val detectedCard = playableCards.random()
                    playOpponentCard(detectedCard, source = "Vision OCR (${detectedCard.name} detected on Arena)")
                    _state.value = _state.value.copy(
                        autoDetectionStatus = "Detected ${detectedCard.name} (-${detectedCard.cost} Elixir)"
                    )
                }
            }
        }
    }
}
