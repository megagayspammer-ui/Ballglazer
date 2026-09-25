package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.elixir.ElixirEngine
import com.example.elixir.ElixirMultiplier
import com.example.model.CardRole
import com.example.model.ClashCardDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Elixir Pro", appName)
  }

  @Test
  fun `clash royale card database integrity and roles`() {
    val pekka = ClashCardDatabase.getCardById("pekka")
    assertNotNull(pekka)
    assertEquals("P.E.K.K.A", pekka?.name)
    assertEquals(7, pekka?.cost)
    assertEquals(CardRole.TANK, pekka?.role)

    val golem = ClashCardDatabase.getCardById("golem")
    assertNotNull(golem)
    assertEquals(8, golem?.cost)
    assertTrue(golem?.isWinCondition == true)

    val theLog = ClashCardDatabase.getCardById("the_log")
    assertNotNull(theLog)
    assertEquals(2, theLog?.cost)
    assertEquals(CardRole.SPELL, theLog?.role)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `anti-multi-count debounce and undo safety`() {
    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    val engine = ElixirEngine(testScope)

    engine.startMatch()
    engine.adjustOpponentElixir(5.0f) // Opponent has 10 elixir
    assertEquals(10.0f, engine.state.value.opponentElixir, 0.01f)

    val pekka = ClashCardDatabase.getCardById("pekka")!!

    // First tap on PEKKA succeeds: 10 - 7 = 3.0
    val firstTapSuccess = engine.playOpponentCard(pekka)
    assertTrue(firstTapSuccess)
    assertEquals(3.0f, engine.state.value.opponentElixir, 0.01f)
    assertEquals(1, engine.state.value.cardHistory.size)

    // Immediate duplicate tap on PEKKA within debounce window fails! (Anti-Multi-Count)
    val secondTapSuccess = engine.playOpponentCard(pekka)
    assertFalse(secondTapSuccess)
    assertEquals(3.0f, engine.state.value.opponentElixir, 0.01f)
    assertEquals(1, engine.state.value.cardHistory.size) // Not counted twice!

    // Testing Undo: refunds 7 elixir and clears event
    val undoSuccess = engine.undoLastCardPlay()
    assertTrue(undoSuccess)
    assertEquals(10.0f, engine.state.value.opponentElixir, 0.01f)
    assertEquals(0, engine.state.value.cardHistory.size)
    assertEquals(0, engine.state.value.opponentDeck.size)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `elixir engine deductions and cycle status`() {
    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    val engine = ElixirEngine(testScope)

    engine.startMatch()
    assertEquals(5.0f, engine.state.value.opponentElixir, 0.01f)

    // Opponent plays PEKKA (-7, capped at 0 from 5)
    val pekka = ClashCardDatabase.getCardById("pekka")!!
    engine.playOpponentCard(pekka)
    assertEquals(0.0f, engine.state.value.opponentElixir, 0.01f)
    assertEquals(1, engine.state.value.opponentDeck.size)

    // Adjust elixir by +10
    engine.adjustOpponentElixir(10f)
    assertEquals(10.0f, engine.state.value.opponentElixir, 0.01f)

    // Opponent plays 4 more cards: Hog, Fireball, The Log, Zap
    val hog = ClashCardDatabase.getCardById("hog_rider")!!
    val fireball = ClashCardDatabase.getCardById("fireball")!!
    val log = ClashCardDatabase.getCardById("the_log")!!
    val zap = ClashCardDatabase.getCardById("zap")!!

    Thread.sleep(160) // satisfy general tap debounce
    engine.playOpponentCard(hog)
    Thread.sleep(160)
    engine.playOpponentCard(fireball)
    Thread.sleep(160)
    engine.playOpponentCard(log)
    Thread.sleep(160)
    engine.playOpponentCard(zap)

    // PEKKA was played 4 cards ago (zap, log, fireball, hog), so in Clash Royale rules, PEKKA is now back in hand!
    val pekkaCycle = engine.state.value.opponentCycleStatuses.find { it.card.id == "pekka" }
    assertNotNull(pekkaCycle)
    assertTrue(pekkaCycle!!.inHand)

    // Zap was just played (0 cards ago), so it is not in hand (4 away)
    val zapCycle = engine.state.value.opponentCycleStatuses.find { it.card.id == "zap" }
    assertNotNull(zapCycle)
    assertFalse(zapCycle!!.inHand)
    assertEquals("4 away", zapCycle.displayStatus)

    // Multiplier switch
    engine.setMultiplier(ElixirMultiplier.TWO_X)
    assertEquals(ElixirMultiplier.TWO_X, engine.state.value.multiplier)
  }
}
