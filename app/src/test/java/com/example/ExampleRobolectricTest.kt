package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.elixir.ElixirEngine
import com.example.elixir.ElixirMultiplier
import com.example.model.ClashCardDatabase
import com.example.model.DeviceType
import com.example.model.NearbyDevice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
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
    assertEquals("OmniHub", appName)
  }

  @Test
  fun `clash royale card database integrity`() {
    val pekka = ClashCardDatabase.getCardById("pekka")
    assertNotNull(pekka)
    assertEquals("P.E.K.K.A", pekka?.name)
    assertEquals(7, pekka?.cost)

    val golem = ClashCardDatabase.getCardById("golem")
    assertNotNull(golem)
    assertEquals(8, golem?.cost)

    val theLog = ClashCardDatabase.getCardById("the_log")
    assertNotNull(theLog)
    assertEquals(2, theLog?.cost)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `elixir engine deductions and multiplier states`() {
    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    val engine = ElixirEngine(testScope)

    engine.startMatch()
    assertEquals(5.0f, engine.state.value.opponentElixir, 0.01f)

    // Deduct 7 for PEKKA from 5 (drops to 0)
    val pekka = ClashCardDatabase.getCardById("pekka")!!
    engine.playOpponentCard(pekka)
    assertEquals(0.0f, engine.state.value.opponentElixir, 0.01f)

    // Adjust elixir by +10
    engine.adjustOpponentElixir(10f)
    assertEquals(10.0f, engine.state.value.opponentElixir, 0.01f)

    // Deduct 4 for Hog Rider
    val hog = ClashCardDatabase.getCardById("hog_rider")!!
    engine.playOpponentCard(hog)
    assertEquals(6.0f, engine.state.value.opponentElixir, 0.01f)

    // Multiplier switch
    engine.setMultiplier(ElixirMultiplier.TWO_X)
    assertEquals(ElixirMultiplier.TWO_X, engine.state.value.multiplier)
  }

  @Test
  fun `nearby device custom nickname and distance math`() {
    val device = NearbyDevice(
      id = "AA:BB:CC:DD:EE:FF",
      rawName = "Wireless Headset",
      customAlias = "My Beats Studio",
      deviceType = DeviceType.BLUETOOTH_LE,
      rssi = -60,
      bearingDegrees = 45f,
      txPower = -59
    )

    assertEquals("My Beats Studio", device.displayName)
    assertTrue(device.hasCustomName)
    assertTrue(device.estimatedDistanceMeters > 0.3)
    assertTrue(device.estimatedDistanceMeters < 5.0)
  }
}
