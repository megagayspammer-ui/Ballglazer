# Elixir Pro ⚡

> Professional esports-grade Clash Royale companion for live elixir tracking, 4-card cycle rotation analysis, anti-multi-count protection, and in-game floating overlay.

---

## 📱 Download & Install APK

A prebuilt, ready-to-install Android APK is included directly inside this repository:

👉 **[Download Elixir-Pro.apk](./apk/Elixir-Pro.apk)** (File path: `apk/Elixir-Pro.apk`)

### Installation Steps on Android:
1. Download `Elixir-Pro.apk` directly to your Android device from this repo.
2. Tap the downloaded file in your notification tray or **Files/Downloads** manager.
3. If prompted by Android, grant **Install unknown apps** permission for your browser or file manager.
4. Tap **Install** and launch **Elixir Pro**!

---

## 🛡️ Anti-Multi-Count Protection System

To guarantee 100% reliable elixir counting without accidental miscounts, **Elixir Pro** features four layers of multi-count protection:

1. **Rapid Double-Tap Debounce**:
   - Frantic or accidental double-taps on the same card within **650ms** are automatically suppressed, preventing accidental duplicate deductions.
   - A general 150ms touch debounce prevents screen jitter duplicates.
2. **Cycle Multi-Count Awareness**:
   - In Clash Royale, after an opponent plays a card, it takes **4 other cards** before it can return to their hand.
   - Cards currently in cycle are tagged in amber (e.g. `4 away`, `3 away`), visually distinguishing them from in-hand cards.
3. **Vision Presence Cooldown**:
   - When using the optical Auto Screen Analyzer, a **16-second deployment cooldown** is assigned to any detected troop on field. This prevents long-lived troops (like Golem or P.E.K.K.A walking down the lane) from being scanned repeatedly.
4. **Instant Undo & Refund**:
   - An instant **Undo Last Play** button allows you to immediately refund the exact elixir deducted and roll back rotation state if a wrong card was tapped.

---

## ⚔️ Match Tools & Features

- **10-Segment Tournament Fluid Bar**: Real-time continuous filling with decimal precision (e.g., `7.3 / 10`).
- **Official Regeneration Rates**:
  - **Single Elixir (1X)**: 2.80s per bar (0.357 el/s) for the first 2 minutes.
  - **Double Elixir (2X)**: 1.40s per bar (0.714 el/s) from 2:00 to 4:00.
  - **Triple Elixir (3X)**: 0.93s per bar (1.075 el/s) in overtime & sudden death.
- **Elixir Advantage Differential**: Live lead or deficit relative to your own card plays (`+4 Advantage`, `-2 Deficit`).
- **Opponent 8-Card Profile**: Automatically tracks discovered cards up to 8 unique cards (no duplicates) and calculates the opponent's average deck elixir.
- **Floating HUD Overlay (`SYSTEM_ALERT_WINDOW`)**: Draws a minimal, high-visibility counter directly over Clash Royale gameplay.
- **Card Almanac**: Detailed stats, roles, counters, and positive trade tips.

---

## 🛠️ Building from Source

```bash
# Build Debug APK
gradle :app:assembleDebug

# Run Unit & Roborazzi Tests
gradle :app:testDebugUnitTest
```

The compiled APK will be at `app/build/outputs/apk/debug/app-debug.apk`.
