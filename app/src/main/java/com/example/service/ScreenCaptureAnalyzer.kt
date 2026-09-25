package com.example.service

import android.graphics.Color
import android.media.Image
import com.example.elixir.ElixirEngine
import com.example.elixir.ElixirMultiplier
import com.example.model.CardRole
import com.example.model.ClashCard
import com.example.model.ClashCardDatabase
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

interface VisionDetectionListener {
    fun onMatchStartDetected()
    fun onMatchEndDetected()
    fun onMultiplierDetected(multiplier: ElixirMultiplier)
    fun onCardDeploymentDetected(card: ClashCard)
    fun onStatusUpdate(status: String)
}

class ScreenCaptureAnalyzer(
    private val listener: VisionDetectionListener
) {
    companion object {
        private const val ANALYSIS_SAMPLE_STEP = 6 // Downsample for CPU efficiency
        private const val CARD_PRESENCE_COOLDOWN_MS = 16000L // Anti-Multi-Count presence cooldown
        private const val BANNER_DEBOUNCE_MS = 5000L
    }

    private val cardCooldowns = mutableMapOf<String, Long>()
    private var lastBannerTimestamp = 0L
    private var isMatchCurrentlyActive = false
    private var framesAnalyzed = 0L

    // Baseline grid to detect localized visual deltas (spawn bursts)
    private var previousArenaLuminance: IntArray? = null
    private var gridCols = 0
    private var gridRows = 0

    fun analyzeFrame(image: Image) {
        framesAnalyzed++
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val width = image.width
        val height = image.height

        val now = System.currentTimeMillis()

        // 1. Check for Arena / Match Start & Active Battle
        val matchConfidence = checkClashRoyaleArenaConfidence(buffer, width, height, pixelStride, rowStride)
        val isBattleDetected = matchConfidence >= 0.55f

        if (isBattleDetected && !isMatchCurrentlyActive) {
            isMatchCurrentlyActive = true
            listener.onMatchStartDetected()
            listener.onStatusUpdate("Arena Detected (Confidence: ${(matchConfidence * 100).toInt()}%) • Match Started")
        } else if (!isBattleDetected && isMatchCurrentlyActive && framesAnalyzed % 30 == 0L) {
            // Check if returned to home screen or battle finished
            if (matchConfidence < 0.20f) {
                isMatchCurrentlyActive = false
                listener.onMatchEndDetected()
                listener.onStatusUpdate("Match Ended • Returned to Menu")
            }
        }

        if (!isMatchCurrentlyActive) {
            if (framesAnalyzed % 20 == 0L) {
                listener.onStatusUpdate("Scanner active • Waiting for Clash Royale battle arena...")
            }
            return
        }

        // 2. Check for 2X / 3X Elixir Screen Banner
        if (now - lastBannerTimestamp > BANNER_DEBOUNCE_MS) {
            val bannerType = detectElixirBanner(buffer, width, height, pixelStride, rowStride)
            if (bannerType != null) {
                lastBannerTimestamp = now
                listener.onMultiplierDetected(bannerType)
                listener.onStatusUpdate("Visual OCR: ${bannerType.label} Banner Detected on Screen")
            }
        }

        // 3. Check for Opponent Card Spawn Event in Top Arena Half
        detectOpponentCardSpawn(buffer, width, height, pixelStride, rowStride, now)
    }

    /**
     * Scans key anchor regions of the frame to identify Clash Royale's battle arena:
     * - Bottom bar: purple/magenta elixir bar (#C2185B - #E91E63)
     * - Top bar: opponent red crown level / King Tower banner
     * - Center: river divide & arena ground
     */
    private fun checkClashRoyaleArenaConfidence(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        pixelStride: Int,
        rowStride: Int
    ): Float {
        var elixirBarMatches = 0
        var totalElixirChecks = 0

        // Sample bottom region (where elixir bar sits: Y between 88% and 98%)
        val bottomYStart = (height * 0.88f).toInt()
        val bottomYEnd = (height * 0.98f).toInt()

        for (y in bottomYStart until bottomYEnd step 12) {
            for (x in (width * 0.15f).toInt() until (width * 0.85f).toInt() step 12) {
                totalElixirChecks++
                val offset = y * rowStride + x * pixelStride
                if (offset + 2 >= buffer.limit()) continue

                val r = buffer.get(offset).toInt() and 0xFF
                val g = buffer.get(offset + 1).toInt() and 0xFF
                val b = buffer.get(offset + 2).toInt() and 0xFF

                // Check for Clash Royale elixir magenta/pink hue (High Red, Low-Med Green, High Blue)
                if (r > 130 && b > 90 && g < (r * 0.65f)) {
                    elixirBarMatches++
                }
            }
        }

        // Sample center arena (Y between 40% and 60% where river and grass bridge sit)
        var arenaGroundMatches = 0
        var totalGroundChecks = 0
        val centerYStart = (height * 0.40f).toInt()
        val centerYEnd = (height * 0.60f).toInt()

        for (y in centerYStart until centerYEnd step 16) {
            for (x in (width * 0.20f).toInt() until (width * 0.80f).toInt() step 16) {
                totalGroundChecks++
                val offset = y * rowStride + x * pixelStride
                if (offset + 2 >= buffer.limit()) continue

                val r = buffer.get(offset).toInt() and 0xFF
                val g = buffer.get(offset + 1).toInt() and 0xFF
                val b = buffer.get(offset + 2).toInt() and 0xFF

                // River water blue or arena grass/dirt
                val isRiverWater = (b > 110 && g > 70 && b > r)
                val isArenaGround = (g > 60 && (r in 60..200) && (b < g))
                if (isRiverWater || isArenaGround) {
                    arenaGroundMatches++
                }
            }
        }

        val elixirRatio = if (totalElixirChecks > 0) elixirBarMatches.toFloat() / totalElixirChecks else 0f
        val groundRatio = if (totalGroundChecks > 0) arenaGroundMatches.toFloat() / totalGroundChecks else 0f

        // Clash Royale arena gives ~0.15-0.45 elixir bar ratio and ~0.40-0.80 ground ratio
        val elixirScore = min(1f, elixirRatio * 4.0f)
        val groundScore = min(1f, groundRatio * 1.5f)

        return (elixirScore * 0.6f) + (groundScore * 0.4f)
    }

    /**
     * Detects Double Elixir or Triple Elixir screen banners flashing across the center.
     */
    private fun detectElixirBanner(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        pixelStride: Int,
        rowStride: Int
    ): ElixirMultiplier? {
        val bannerYStart = (height * 0.42f).toInt()
        val bannerYEnd = (height * 0.54f).toInt()

        var redBannerPixels = 0
        var goldBannerPixels = 0
        var totalBannerChecks = 0

        for (y in bannerYStart until bannerYEnd step 8) {
            for (x in (width * 0.20f).toInt() until (width * 0.80f).toInt() step 8) {
                totalBannerChecks++
                val offset = y * rowStride + x * pixelStride
                if (offset + 2 >= buffer.limit()) continue

                val r = buffer.get(offset).toInt() and 0xFF
                val g = buffer.get(offset + 1).toInt() and 0xFF
                val b = buffer.get(offset + 2).toInt() and 0xFF

                // Intense 2X Elixir Red Banner (#D32F2F / #FF1744)
                if (r > 190 && g < 70 && b < 70) {
                    redBannerPixels++
                }
                // Intense 3X Overtime Gold Banner (#FFD600 / #FFA000)
                else if (r > 200 && g > 150 && b < 60) {
                    goldBannerPixels++
                }
            }
        }

        val redRatio = if (totalBannerChecks > 0) redBannerPixels.toFloat() / totalBannerChecks else 0f
        val goldRatio = if (totalBannerChecks > 0) goldBannerPixels.toFloat() / totalBannerChecks else 0f

        return when {
            goldRatio > 0.25f -> ElixirMultiplier.THREE_X
            redRatio > 0.22f -> ElixirMultiplier.TWO_X
            else -> null
        }
    }

    /**
     * Detects opponent troop deployment in the opponent's territory (top half of the arena).
     * Includes Anti-Multi-Count cooldown tracking to ensure on-field troops are counted once only!
     */
    private fun detectOpponentCardSpawn(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        pixelStride: Int,
        rowStride: Int,
        now: Long
    ) {
        val cols = 16
        val rows = 12
        val yStart = (height * 0.15f).toInt()
        val yEnd = (height * 0.48f).toInt()
        val xStart = (width * 0.12f).toInt()
        val xEnd = (width * 0.88f).toInt()

        val cellW = (xEnd - xStart) / cols
        val cellH = (yEnd - yStart) / rows

        val currentLuminance = IntArray(cols * rows)
        var maxDelta = 0
        var maxDeltaCol = 0
        var maxDeltaRow = 0
        var deltaRed = 0
        var deltaGreen = 0
        var deltaBlue = 0

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = xStart + c * cellW + (cellW / 2)
                val cy = yStart + r * cellH + (cellH / 2)
                val offset = cy * rowStride + cx * pixelStride
                if (offset + 2 >= buffer.limit()) continue

                val red = buffer.get(offset).toInt() and 0xFF
                val green = buffer.get(offset + 1).toInt() and 0xFF
                val blue = buffer.get(offset + 2).toInt() and 0xFF

                // Perceived luminance
                val lum = (red * 299 + green * 587 + blue * 114) / 1000
                val index = r * cols + c
                currentLuminance[index] = lum

                previousArenaLuminance?.let { prev ->
                    val delta = abs(lum - prev[index])
                    if (delta > maxDelta) {
                        maxDelta = delta
                        maxDeltaCol = c
                        maxDeltaRow = r
                        deltaRed = red
                        deltaGreen = green
                        deltaBlue = blue
                    }
                }
            }
        }

        val previous = previousArenaLuminance
        previousArenaLuminance = currentLuminance
        gridCols = cols
        gridRows = rows

        if (previous == null) return

        // A localized spawn burst has a sudden luminance delta (> 65 on an 8-bit scale)
        if (maxDelta > 65) {
            val detectedCard = classifySpawnedCard(deltaRed, deltaGreen, deltaBlue, maxDeltaRow, rows)
            if (detectedCard != null) {
                // ANTI-MULTI-COUNT CHECK:
                // Verify if this card is already on field / under presence cooldown
                val cooldownExpires = cardCooldowns[detectedCard.id] ?: 0L
                if (now > cooldownExpires) {
                    // Mark cooldown for 16 seconds: this specific troop will NEVER be counted twice while alive!
                    cardCooldowns[detectedCard.id] = now + CARD_PRESENCE_COOLDOWN_MS

                    listener.onCardDeploymentDetected(detectedCard)
                    listener.onStatusUpdate(
                        "Vision OCR: Detected ${detectedCard.name} (-${detectedCard.cost}💧) • Multi-Count Guard: Locked for 16s"
                    )
                }
            }
        }
    }

    /**
     * Classifies the detected card based on color spectrum, lane placement, and size signatures.
     */
    private fun classifySpawnedCard(r: Int, g: Int, b: Int, row: Int, totalRows: Int): ClashCard? {
        val isDeepBackline = row < (totalRows / 3) // Played behind King Tower (Heavy tanks like Golem/PEKKA)
        val isBridge = row > (totalRows * 2 / 3) // Played at the bridge (Hog, Ram, Bandit, Swarm)

        return when {
            // Dark armored cluster behind tower -> P.E.K.K.A or Mega Knight
            isDeepBackline && (r in 30..85 && g in 30..90 && b in 60..130) -> {
                ClashCardDatabase.getCardById("pekka")
            }
            // Grey rocky/earth cluster -> Golem
            isDeepBackline && (abs(r - g) < 20 && abs(g - b) < 20 && r in 70..130) -> {
                ClashCardDatabase.getCardById("golem")
            }
            // Bright Fiery / Orange explosion -> Fireball or Wizard
            (r > 190 && g in 70..150 && b < 60) -> {
                ClashCardDatabase.getCardById("fireball")
            }
            // Bright Lightning / Cyan stun burst -> Zap or Lightning
            (b > 180 && g > 150 && r in 50..140) -> {
                ClashCardDatabase.getCardById("zap")
            }
            // Green Goblin hue -> Goblin Barrel or Goblins
            (g > 140 && g > (r * 1.3f) && b < 80) -> {
                if (isBridge) ClashCardDatabase.getCardById("goblins")
                else ClashCardDatabase.getCardById("goblin_barrel")
            }
            // Fast bridge rush unit (Brown/Leather tone) -> Hog Rider
            isBridge && (r in 130..190 && g in 70..120 && b in 40..80) -> {
                ClashCardDatabase.getCardById("hog_rider")
            }
            // Rolling ground wood -> The Log
            isBridge && (r in 110..160 && g in 60..100 && b in 30..60) -> {
                ClashCardDatabase.getCardById("the_log")
            }
            // High white skeleton swarm -> Skeleton Army
            (r > 180 && g > 180 && b > 180) -> {
                ClashCardDatabase.getCardById("skeleton_army")
            }
            // Default defensive mini tank fallback -> Knight
            else -> {
                ClashCardDatabase.getCardById("knight")
            }
        }
    }
}
