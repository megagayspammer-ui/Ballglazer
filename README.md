# NearMap Radar 📡

> Real-time RF proximity radar & spatial sonar for nearby Bluetooth (BLE & Classic) and Wi-Fi devices with customizable nicknames, orientation compass sync, and live signal telemetry.

---

## 📱 Download & Install APK

A prebuilt, ready-to-install Android APK is included directly inside this repository:

👉 **[Download NearMap-Radar.apk](./apk/NearMap-Radar.apk)** (File path: `apk/NearMap-Radar.apk`)

### Installation Steps on Android:
1. Download `NearMap-Radar.apk` directly to your Android device from this repo.
2. Tap the downloaded file in your notification tray or **Files/Downloads** manager.
3. If prompted by Android, grant **Install unknown apps** permission for your browser or file manager.
4. Tap **Install** and launch **NearMap Radar**!

---

## 🌟 Key Features

- **Polar Sonar & Spatial Proximity Map**:
  - Live 360° sweeping radar with phosphor trailing glow.
  - Concentric range distance rings (5m, 15m, 25m, 50m) with automatic distance scaling.
  - Interactive radar blips: Tap any detected blip to view live telemetry and assign names.
- **Hardware Sensor Compass Sync**:
  - Integrated with the device's magnetometer and rotation vector sensors.
  - Turn and rotate your phone physically to orient the radar map to match your real-world heading.
- **Custom Device Nicknames & Local Persistence**:
  - Give any detected Bluetooth or Wi-Fi device a friendly alias (e.g., *"My Laptop"*, *"Living Room TV"*, *"Office Wi-Fi"*, *"Noise-Canceling Headphones"*).
  - Preserved locally across app launches using Room Database (`DeviceAliasEntity`).
  - Saved nicknames automatically highlight devices in glowing emerald green on the radar screen.
- **Live Signal Telemetry & Distance Physics**:
  - **Log-Distance Path Loss Model**: Estimates physical distance ($d = 10^{\frac{TxPower - RSSI}{10 \cdot n}}$) in both meters and feet.
  - **Live RSSI Sparkline Graph**: Real-time signal fluctuation chart tracking RF variations over time.
  - Proximity categories: *Immediate (< 1.5m)*, *Near (1.5 - 5m)*, *Mid-Range (5 - 15m)*, and *Far (> 15m)*.
- **Transparent RF Accuracy Notice**:
  - Educational guidance explaining why RF signal strength (RSSI) fluctuates with physical walls, obstacles, and human body attenuation.
- **Dual View Modes**:
  - Switch seamlessly between the **Radar Sonar** map and the categorized **Device List**.
  - Quick filter chips: *All*, *Bluetooth*, *Wi-Fi*, and *Named Only*.
  - Search by alias, broadcast name, MAC address, or notes.

---

## 🛠️ Building from Source

To compile the APK yourself using Android Studio or command-line Gradle:

```bash
# Clone the repository
git clone <your-repo-url>
cd <repo-folder>

# Build the debug APK
gradle :app:assembleDebug
```

The resulting APK will be placed at `app/build/outputs/apk/debug/app-debug.apk`.
