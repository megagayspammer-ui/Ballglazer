package com.example.elixir

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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

enum class ElixirMultiplier(val label: String, val secondsPerElixir: Float) {
    ONE_X("1X Elixir", 2.8f),
    TWO_X("2X Elixir", 1.4f),
    THREE_X("3X Elixir", 0.93f);

    val elixirPerSecond: Float get() = 1f / secondsPerElixir
}

data class CardPlayEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val card: ClashCard,
    val elixirBefore: Float,
    val elixirAfter: Float,
    val timestampMs: Long = System.currentTimeMillis(),
    val matchSecond: Int = 0,
    val source: String = "Manual",
    val isOpponent: Boolean = true
)

data class CardCycleStatus(
    val card: ClashCard,
    val cardsAgo: Int, // 0 = just played (4 away), 1 = 3 away, 2 = 2 away, 3 = 1 away, 4+ = in hand
    val inHand: Boolean
) {
    val displayStatus: String
        get() = if (inHand) "IN HAND" else "${max(1, 4 - cardsAgo)} away"
}

data class ElixirTrackerState(
    val opponentElixir: Float = 5.0f,
    val userElixir: Float = 5.0f,
    val multiplier: ElixirMultiplier = ElixirMultiplier.ONE_X,
    val isMatchRunning: Boolean = false,
    val matchElapsedMs: Long = 0L,
    val isAutoDetectionEnabled: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false,
    val isMediaProjectionActive: Boolean = false,
    val autoDetectionStatus: String = "Ready for live Clash Royale screen capture",
    val cardHistory: List<CardPlayEvent> = emptyList(),
    val opponentDeck: List<ClashCard> = emptyList(),
    val detectedMultiplierTag: String? = null,
    val isOpponentLeaking: Boolean = false,
    val hapticEnabled: Boolean = true,
    val autoStartOnFirstPlay: Boolean = true,
    val antiMultiCountActive: Boolean = true,
    val lastNoticeMessage: String? = null
) {
    val matchTimeSeconds: Int
        get() = (matchElapsedMs / 1000L).toInt()

    val formattedTime: String
        get() {
            val totalSec = matchTimeSeconds
            val min = totalSec / 60
            val sec = totalSec % 60
            return String.format(java.util.Locale.US, "%02d:%02d", min, sec)
        }

    val elixirAdvantage: Int
        get() = (userElixir - opponentElixir).roundToInt()

    val roundedOpponentElixir: Int
        get() = min(10, max(0, opponentElixir.toInt()))

    val roundedUserElixir: Int
        get() = min(10, max(0, userElixir.toInt()))

    /**
     * Clash Royale 4-card cycle tracking for discovered opponent cards.
     */
    val opponentCycleStatuses: List<CardCycleStatus>
        get() {
            val opponentPlays = cardHistory.filter { it.isOpponent }
            return opponentDeck.map { card ->
                val lastIndex = opponentPlays.indexOfFirst { it.card.id == card.id }
                if (lastIndex == -1) {
                    CardCycleStatus(card, cardsAgo = 999, inHand = true)
                } else {
                    val inHand = lastIndex >= 4
                    CardCycleStatus(card, cardsAgo = lastIndex, inHand = inHand)
                }
            }
        }
}

class ElixirEngine(
    private val scope: CoroutineScope,
    private val context: Context? = null
) {
    companion object {
        private const val MIN_SAME_CARD_TAP_DEBOUNCE_MS = 650L
        private const val MIN_GENERAL_TAP_DEBOUNCE_MS = 150L
    }

    private val _state = MutableStateFlow(ElixirTrackerState())
    val state: StateFlow<ElixirTrackerState> = _state.asStateFlow()

    private var tickerJob: Job? = null
    private var lastLeakAlertTimestamp = 0L

    // Anti-Multi-Count Guards
    private var lastTappedCardId: String? = null
    private var lastTappedTimestamp = 0L

    fun startMatch() {
        _state.value = _state.value.copy(
            isMatchRunning = true,
            opponentElixir = 5.0f,
            userElixir = 5.0f,
            matchElapsedMs = 0L,
            multiplier = ElixirMultiplier.ONE_X,
            cardHistory = emptyList(),
            opponentDeck = emptyList(),
            detectedMultiplierTag = null,
            isOpponentLeaking = false,
            lastNoticeMessage = "Match started (5.0 Elixir)"
        )
        startTicker()
    }

    fun pauseOrResumeMatch() {
        val current = _state.value
        val newRunning = !current.isMatchRunning
        _state.value = current.copy(
            isMatchRunning = newRunning,
            lastNoticeMessage = if (newRunning) "Match resumed" else "Match paused"
        )
        if (newRunning) {
            startTicker()
        } else {
            tickerJob?.cancel()
        }
    }

    fun resetMatch() {
        tickerJob?.cancel()
        lastTappedCardId = null
        lastTappedTimestamp = 0L
        _state.value = ElixirTrackerState(
            isOverlayPermissionGranted = _state.value.isOverlayPermissionGranted,
            isAutoDetectionEnabled = _state.value.isAutoDetectionEnabled,
            isMediaProjectionActive = _state.value.isMediaProjectionActive,
            hapticEnabled = _state.value.hapticEnabled,
            autoStartOnFirstPlay = _state.value.autoStartOnFirstPlay,
            lastNoticeMessage = "Match reset"
        )
    }

    fun setMultiplier(multiplier: ElixirMultiplier) {
        _state.value = _state.value.copy(
            multiplier = multiplier,
            detectedMultiplierTag = if (multiplier != ElixirMultiplier.ONE_X) multiplier.label else null,
            lastNoticeMessage = "Multiplier switched to ${multiplier.label}"
        )
    }

    fun setHapticEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(hapticEnabled = enabled)
    }

    fun setAutoStartOnFirstPlay(enabled: Boolean) {
        _state.value = _state.value.copy(autoStartOnFirstPlay = enabled)
    }

    /**
     * Plays an opponent card with built-in Anti-Multi-Count debounce protection.
     * Prevents accidental double-taps on the same card within 650ms.
     */
    fun playOpponentCard(card: ClashCard, source: String = "Manual"): Boolean {
        val now = System.currentTimeMillis()

        // 1. Check Anti-Multi-Count Debounce
        if (_state.value.antiMultiCountActive) {
            if (card.id == lastTappedCardId && (now - lastTappedTimestamp) < MIN_SAME_CARD_TAP_DEBOUNCE_MS) {
                _state.value = _state.value.copy(
                    lastNoticeMessage = "Suppressed rapid duplicate on ${card.name} (Anti-Multi-Count)"
                )
                return false
            }
            if ((now - lastTappedTimestamp) < MIN_GENERAL_TAP_DEBOUNCE_MS) {
                return false
            }
        }

        lastTappedCardId = card.id
        lastTappedTimestamp = now

        if (!_state.value.isMatchRunning && _state.value.autoStartOnFirstPlay) {
            startMatch()
        }

        vibrateShort()

        val current = _state.value
        val before = current.opponentElixir
        val after = max(0f, before - card.cost)

        // 2. Prevent multi-counting the same card in the 8-card deck profile
        val updatedDeck = if (current.opponentDeck.none { it.id == card.id } && current.opponentDeck.size < 8) {
            current.opponentDeck + card
        } else {
            current.opponentDeck
        }

        val event = CardPlayEvent(
            card = card,
            elixirBefore = before,
            elixirAfter = after,
            matchSecond = current.matchTimeSeconds,
            source = source,
            isOpponent = true
        )

        _state.value = current.copy(
            opponentElixir = after,
            opponentDeck = updatedDeck,
            cardHistory = listOf(event) + current.cardHistory.take(39),
            lastNoticeMessage = "Played ${card.name} (-${card.cost}💧)"
        )
        return true
    }

    fun playUserCard(card: ClashCard) {
        val now = System.currentTimeMillis()
        if (_state.value.antiMultiCountActive && (now - lastTappedTimestamp) < MIN_GENERAL_TAP_DEBOUNCE_MS) {
            return
        }
        lastTappedTimestamp = now

        vibrateShort()
        val current = _state.value
        val before = current.userElixir
        val after = max(0f, before - card.cost)

        val event = CardPlayEvent(
            card = card,
            elixirBefore = before,
            elixirAfter = after,
            matchSecond = current.matchTimeSeconds,
            source = "My Card",
            isOpponent = false
        )

        _state.value = current.copy(
            userElixir = after,
            cardHistory = listOf(event) + current.cardHistory.take(39),
            lastNoticeMessage = "You played ${card.name} (-${card.cost}💧)"
        )
    }

    fun undoLastCardPlay(): Boolean {
        val current = _state.value
        if (current.cardHistory.isEmpty()) return false

        val lastEvent = current.cardHistory.first()
        val remainingHistory = current.cardHistory.drop(1)

        vibrateShort()

        if (lastEvent.isOpponent) {
            val refundedElixir = min(10f, current.opponentElixir + lastEvent.card.cost)
            val remainingOpponentCardIds = remainingHistory.filter { it.isOpponent }.map { it.card.id }.toSet()
            val recomputedDeck = current.opponentDeck.filter { remainingOpponentCardIds.contains(it.id) }

            _state.value = current.copy(
                opponentElixir = refundedElixir,
                opponentDeck = recomputedDeck,
                cardHistory = remainingHistory,
                lastNoticeMessage = "Undid ${lastEvent.card.name} (+${lastEvent.card.cost}💧 refunded)"
            )
        } else {
            val refundedUser = min(10f, current.userElixir + lastEvent.card.cost)
            _state.value = current.copy(
                userElixir = refundedUser,
                cardHistory = remainingHistory,
                lastNoticeMessage = "Undid your ${lastEvent.card.name} (+${lastEvent.card.cost}💧 refunded)"
            )
        }
        return true
    }

    fun adjustOpponentElixir(delta: Float) {
        vibrateShort()
        val current = _state.value
        val newElixir = (current.opponentElixir + delta).coerceIn(0f, 10f)
        _state.value = current.copy(
            opponentElixir = newElixir,
            lastNoticeMessage = if (delta > 0) "+${delta.toInt()} Opponent Elixir" else "${delta.toInt()} Opponent Elixir"
        )
    }

    fun adjustUserElixir(delta: Float) {
        vibrateShort()
        val current = _state.value
        val newElixir = (current.userElixir + delta).coerceIn(0f, 10f)
        _state.value = current.copy(
            userElixir = newElixir,
            lastNoticeMessage = if (delta > 0) "+${delta.toInt()} My Elixir" else "${delta.toInt()} My Elixir"
        )
    }

    fun updateVisionStatus(status: String) {
        _state.value = _state.value.copy(
            autoDetectionStatus = status,
            lastNoticeMessage = status
        )
    }

    fun setMediaProjectionActive(active: Boolean) {
        _state.value = _state.value.copy(
            isMediaProjectionActive = active,
            isAutoDetectionEnabled = active,
            autoDetectionStatus = if (active) "Live Screen Capture & Frame Analyzer Active" else "Screen Analyzer Stopped"
        )
    }

    fun setOverlayPermissionGranted(granted: Boolean) {
        _state.value = _state.value.copy(isOverlayPermissionGranted = granted)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            val tickIntervalMs = 50L
            while (isActive) {
                delay(tickIntervalMs)
                if (!_state.value.isMatchRunning) continue

                val current = _state.value
                val regenPerTick = (current.multiplier.elixirPerSecond * (tickIntervalMs / 1000f))

                val newOpponentElixir = min(10f, current.opponentElixir + regenPerTick)
                val newUserElixir = min(10f, current.userElixir + regenPerTick)
                val newElapsedMs = current.matchElapsedMs + tickIntervalMs
                val totalSeconds = (newElapsedMs / 1000L).toInt()

                // Authentic Clash Royale overtime / match phases:
                // 0:00 - 2:00: 1X Elixir (2.80s/bar)
                // 2:00 - 4:00: 2X Elixir (1.40s/bar)
                // 4:00 - 5:00: 3X Elixir (0.93s/bar in Sudden Death)
                val updatedMultiplier = when {
                    totalSeconds >= 240 -> ElixirMultiplier.THREE_X
                    totalSeconds >= 120 -> ElixirMultiplier.TWO_X
                    else -> current.multiplier
                }

                val isLeaking = newOpponentElixir >= 9.95f
                if (isLeaking && System.currentTimeMillis() - lastLeakAlertTimestamp > 3000L) {
                    lastLeakAlertTimestamp = System.currentTimeMillis()
                    vibrateLeakAlert()
                }

                _state.value = current.copy(
                    opponentElixir = newOpponentElixir,
                    userElixir = newUserElixir,
                    matchElapsedMs = newElapsedMs,
                    multiplier = updatedMultiplier,
                    isOpponentLeaking = isLeaking
                )
            }
        }
    }

    private fun vibrateShort() {
        if (!_state.value.hapticEnabled || context == null) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25L)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateLeakAlert() {
        if (!_state.value.hapticEnabled || context == null) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70L)
            }
        } catch (_: Exception) {}
    }
}
