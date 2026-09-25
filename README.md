# OmniHub ⚡

> Multi-utility super application featuring **NearMap RF Radar**, **Clash Royale Elixir Master** (card database, 1X/2X/3X regeneration tracking, manual/auto card deduction, and floating HUD overlay), and **Local On-Device AI** (GGUF model downloader & offline LLM inference).

---

## 📱 Download & Install APK

A prebuilt, ready-to-install Android APK is included directly inside this repository:

👉 **[Download OmniHub.apk](./apk/OmniHub.apk)** (File path: `apk/OmniHub.apk`)

### Installation Steps on Android:
1. Download `OmniHub.apk` directly to your Android device from this repo.
2. Tap the downloaded file in your notification tray or **Files/Downloads** manager.
3. If prompted by Android, grant **Install unknown apps** permission for your browser or file manager.
4. Tap **Install** and launch **OmniHub**!

---

## 🌟 Modules in OmniHub

### 1. ⚔️ Clash Royale Elixir Master & Floating Overlay
- **Card Database**: Full stats, elixir costs, types (Troops, Spells, Buildings), rarities, and emoji icons for cards across all elixir costs (1 to 9).
- **Real-Time Elixir Physics**:
  - Accurate 10-segment fluid elixir gauge.
  - Matches authentic regeneration rates: 1X (2.8s per elixir bar), 2X (1.4s per bar at 2:00 mark), and 3X (0.93s per bar in triple elixir / overtime).
  - Tracks **Elixir Advantage** (lead/deficit relative to your own supply) and warns if opponent is leaking elixir.
- **Manual Mode**: Tap any card card in the arena deck grid to immediately subtract that card's cost (e.g., tap P.E.K.K.A → -7 Elixir; tap Hog Rider → -4 Elixir).
- **Auto Screen Analyzer**: Optical detection engine watching for card deployment events on screen and automatically detecting 2X/3X Elixir banners to accelerate regeneration.
- **Floating HUD Overlay**: Uses `SYSTEM_ALERT_WINDOW` permission to float directly over Clash Royale gameplay so you never have to tab out.
- **Match Play History**: Chronological log of deployed enemy cards with timestamps and elixir deltas.

### 2. 🧠 Local AI Model Hub & Offline Inference
- **GGUF Model Downloader**: Browse and download popular quantized open weights models directly onto local device storage:
  - *Qwen 2.5 (0.5B Chat)* - Ultra-lightweight & lightning fast.
  - *TinyLlama (1.1B Chat)* - High-efficiency general reasoning.
  - *Gemma 2 (2B Instruct)* - Google DeepMind analytical weights.
  - *Phi-3 Mini (3.8B 4K)* - State-of-the-art small language model.
- **Offline Inference Playground**: Chat with loaded models completely offline with streaming token output.
- **Parameter Controls**: Adjust temperature (0.1 - 1.5) and customize system prompts.
- **Storage Management**: Visual breakdown of model cache vs. free device storage.

### 3. 📡 NearMap RF Radar & Device Proximity
- **360° Sonar Scanner**: Sweeping beam tracking nearby Bluetooth (BLE & Classic) and Wi-Fi networks.
- **Custom Nicknames**: Give any detected device a friendly name (e.g., *"My Laptop"*, *"Living Room TV"*), saved permanently in a local **Room Database**.
- **Hardware Compass Sync**: Point and rotate your phone physically to orient the radar sweep to real-world magnetic north.
- **Log-Distance RF Path Loss**: Calculates physical proximity in meters and feet with live RSSI sparkline telemetry.

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone <your-repo-url>
cd <repo-folder>

# Build the debug APK
gradle :app:assembleDebug
```

The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.
