# Mathlator

<p align="center">
  <img src="https://raw.githubusercontent.com/MrGokulaKrishnan/Mathlator/main/art/mathlator_banner.png" alt="Mathlator Banner" width="400" onerror="this.style.display='none'"/>
</p>

<p align="center">
  <strong>High-Precision Android Calculator, Unit Converter & Finance Instrument</strong><br/>
  <em>Engineered in a bespoke Neumorphic Tactical Dark hardware aesthetic with pure black and vibrant orange accents.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.1.20-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.1.20" />
  <img src="https://img.shields.io/badge/Android-SDK%2026%2B%20%7C%20Target%2035-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white" alt="Android SDK" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.05.00-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose BOM" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-FF5E00.svg?style=for-the-badge" alt="Clean Architecture" />
  <img src="https://img.shields.io/badge/Offline--First-100%25%20Private-10B981.svg?style=for-the-badge" alt="Offline First" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge" alt="License" />
</p>

---

## 📌 Overview

**Mathlator** (`com.mathlator.app`) is a flagship, offline-first Android calculation workstation combining a scientific calculator, 13 physical converters, specialized biometrics/chrono tools, and 4 comprehensive finance instruments into a single cohesive app.

Built entirely from scratch in modern **Kotlin** and **Jetpack Compose**, Mathlator rejects generic flat designs in favor of a tactile **"Neumorphic Tactical Dark"** hardware instrument aesthetic: sunken OLED telemetry displays, extruded dual-shadow physical keycaps, glowing neon focus states, and seamless edge-to-edge system bars.

---

## ⚡ Core Capabilities

### 1. 🧮 Advanced Scientific Calculator
- **Recursive Descent Math Engine:** Custom zero-dependency tokenizer and parser supporting operator precedence (`^` right-associativity binding tighter than unary minus: `-2^2 = -4`, `2^3^2 = 512`), implicit multiplication (`2(3) = 6`), nested parentheses, and auto-closing brackets for real-time live result previews.
- **Scientific Ribbon `f(x)`:** Expandable function panel offering trigonometry (`sin`, `cos`, `tan`), hyperbolic functions (`sinh`, `cosh`, `tanh`), inverse functions, logarithms (`ln`, `log10`), powers, roots, constants ($\pi$, $e$), and angle switching (`DEG` / `RAD`).
- **OLED Telemetry Display:** High-contrast recessed readout with micro telemetry badge (`CALC // DEG`), auto-scaling tabular figures (`tnum`), swipe-to-backspace gesture, and haptic feedback.
- **5×4 Ergonomic Keypad:** High-depth extruded keycaps with tactile dual-shadow physics and physical 0.96× depression scaling on press.

### 2. 📐 13 Precision Converters + Specialized Tools
Presented in an interactive 3-column Neumorphic Bento Grid with etched sockets:
- **Length, Mass, Temperature, Speed, Volume, Area, Time, Data Storage, Fuel Economy, Pressure, Energy, Power, Angle.**
- **Dual Recessed I/O Wells:** Dual input/output cards with active orange glowing focus rings, floating center 3D swap orb, and expandable all-units drawer.
- **Uniform 4×4 Tactical Converter Keypad:** Dedicated hardware layout (`[7, 8, 9, ⌫]`, `[4, 5, 6, C]`, `[1, 2, 3, ⇅]`, `[., 0, 00, 📋]`) with zero empty dead cells.
- **Specialized Instruments:**
  - **Age Chrono Telemetry:** Dual date selection wells, hero telemetry card with glowing left accent bar, 2×2 bento breakdown (Years, Months, Weeks, Days, Hours), and animated next-birthday countdown bar.
  - **Biometric BMI Spectrum:** Metric/Imperial units segmented toggle, dual height/weight stepper cards, Asian WHO cutoffs switch, and 4-zone biometric arc gauge with animated needle.
  - **Numeral Systems:** Real-time conversion across Hexadecimal, Decimal, Octal, and Binary.
  - **Discount Calculator:** Quick calculations for original price, discount percentage, extra tax, and final savings.
  - **Date Difference:** Exact duration between two calendar dates in days, weeks, and months.

### 3. 📈 4 Finance Instruments
- **Currency 3-Slot Rack:**
  - Active MAIN input slot + 2 read-only target conversion slots with live rate captions.
  - Quick-promote `⇅` button to instantly swap any target currency to MAIN.
  - Resilient dual-source networking: `open.er-api.com` (primary, zero API key) with automatic fallback to `api.frankfurter.dev`.
  - Offline Room database caching with 12-hour TTL and live sync telemetry header.
- **Quick GST Split Module:**
  - Embedded interactive tool on the Finance Hub with tactile slab selector pills (`0%`, `5%`, `12%`, `18%`, `28%`).
  - Real-time split breakdown of Net Base, CGST, SGST, and Gross Total.
  - Supports both **Add GST** and **Remove GST** calculations.
- **Investment Calculators:**
  - **SIP Calculator:** Monthly systematic investment plans with compounding returns.
  - **Lump Sum & Fixed Deposit (FD):** Wealth projection over customizable tenures.
- **Loan & EMI Calculator:**
  - Monthly payment calculation, total interest payable, and overall loan repayment breakdown.

---

## 🎨 Neumorphic Tactical Dark Design System

Mathlator features a bespoke hardware instrument appearance powered by custom Jetpack Compose Canvas modifiers:

| Token | Hex / Spec | Description |
|---|---|---|
| `ObsidianChassis` | `#08090C` | Root matte chassis background |
| `SurfaceRecessedWell` | `#0B0D12` | Sunken telemetry readouts and input wells |
| `SurfaceRecessedWellActive`| `#0E1017` | Focused input cavity background |
| `SurfaceCard` | `#11141B` | Elevated Neumorphic Bento tiles |
| `KeycapNumBase` | `#151821` | Physical numeric keycap fill |
| `KeycapOpBase` | `#1E2330` | Arithmetic operator keycap fill |
| `KeycapUtilBase` | `#13161F` | Utility keycap fill (`C`, `⌫`, `⇅`, `📋`) |
| `TacticalOrange` | `#FF5E00` | Primary hardware indicator & action accent |
| `TacticalAmber` | `#FF9E00` | Secondary gradient accent & warning highlight |
| `EmeraldTelemetry` | `#10B981` | Real-time network sync indicator & success state |

### Hardware Canvas Engine (`TacticalModifiers.kt`)
- `Modifier.tacticalExtrudedKeycap`: Dual-offset shadows (bottom-right `+5dp, +6dp` shadow with `12dp` blur, top-left `-3dp, -3dp` ambient highlight with `8dp` blur, specular border stroke, inner recessed shadow on touch, and 0.96× scale depression).
- `Modifier.tacticalRecessedWell`: Sunken display simulation with dark inner corner gradients and glowing `1.5dp` `#FF5E00` outline on focus.
- `Modifier.tacticalCard` & `Modifier.tacticalSocket`: Extruded Bento cards and etched recessed icon sockets.

---

## 🏛️ Architecture & Tech Stack

Mathlator is engineered strictly according to **Clean Architecture** and **Modern Android Architecture (MVVM + UDF)**:

```
com.mathlator.app
├── MainActivity.kt               # Edge-to-edge container, theme-aware system bars
├── MathlatorApp.kt               # Dagger-Hilt application entry point
├── core/
│   ├── engine/                   # Pure Kotlin math engine (Tokenizer, Parser, Evaluator)
│   ├── format/                   # NumberFormatter (Indian & International digit grouping)
│   └── finance/                  # Financial math engines (GST, EMI, SIP, FD, Currency)
├── data/
│   ├── db/                       # Room 2.7.1 Database, HistoryDao, RateDao, Entities
│   ├── prefs/                    # SettingsRepository (Jetpack DataStore Preferences)
│   └── rates/                    # Retrofit 2.11 + OkHttp currency rate providers & repo
├── feature/
│   ├── calculator/               # Scientific Calculator Screen, Keypad, SciPanel, ViewModel
│   ├── converters/               # Bento Hub, UnitConverterScreen, AgeScreen, BmiScreen
│   ├── finance/                  # Finance Hub, Quick GST, Currency 3-Slot Screen
│   ├── history/                  # Calculation History Screen & ViewModel
│   └── settings/                 # App Settings & About Screen
└── ui/
    ├── theme/                    # TacticalColor, TacticalDesignSystem, Type, Theme
    ├── components/               # TacticalModifiers, TacticalIcons, TacticalKeypads, ResultCard
    └── navigation/               # MathlatorNavHost, Top Segmented Dock, Route Navigation
```

### Technology Matrix
- **Language & JVM:** Kotlin 2.1.20, Java 17 (`jvmTarget = "17"`), Java 8+ API desugaring (`desugar_jdk_libs:2.1.4`)
- **Android Targets:** `compileSdk = 36`, `targetSdk = 35`, `minSdk = 26`
- **UI Framework:** Jetpack Compose (BOM `2025.05.00`), Material 3, Navigation Compose `2.9.0`
- **Dependency Injection:** Dagger Hilt `2.56` + `hilt-navigation-compose:1.2.0`
- **Persistence:** Room `2.7.1` (KSP `2.1.20-1.0.32`), DataStore Preferences `1.1.4`
- **Networking:** OkHttp `4.12.0`, Retrofit `2.11.0`, Kotlinx Serialization `1.8.1`
- **Asynchrony:** KotlinX Coroutines `1.10.2` with `StateFlow` and `SharedFlow`
- **Build System:** Gradle 8.11.1, Android Gradle Plugin 8.9.1, Gradle Version Catalog (`libs.versions.toml`)

---

## 🛡️ Privacy & Offline Guarantee

- **Zero Ads & Zero Tracking:** No third-party SDKs, analytics, ads, accounts, or user profiling.
- **Offline-First:** All mathematical calculations, unit conversions, age computations, and finance math execute 100% locally on the device CPU.
- **Minimal Network Scope:** Network access is exclusively utilized by the Currency Converter to fetch exchange rates, cached locally for offline use.

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug (2024.2+) or command-line Android SDK with SDK 36.
- JDK 17 configured as `JAVA_HOME`.

### 1. Build Debug APK & Automatic Stamping
The included build script compiles the project, runs verification, copies the latest artifact to `dist/Mathlator-latest.apk`, and generates a stamped version tagged with the commit hash:

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-apk.ps1
```

**macOS / Linux:**
```bash
./scripts/build-apk.sh
```

### 2. Install on Device via ADB
```powershell
adb install -r -d dist/Mathlator-latest.apk
adb shell am start -n com.mathlator.app.debug/com.mathlator.app.MainActivity
```

### 3. Local Wi-Fi Device Download Server
Host the APK over your local Wi-Fi network so any phone on the same network can download and test it directly in browser:
```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-apk.ps1 -Serve
```

### 4. Run Test Suite & Lint Verification
```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify.ps1
```
Or directly via Gradle:
```bash
./gradlew testDebugUnitTest lintDebug
```

---

## 📜 Key Architectural Decisions

1. **Pure Kotlin Engine:** Core evaluation logic resides in `core/` with zero Android framework imports, enabling sub-second unit test execution.
2. **Single-Point Inset Consumption:** Root tab layout consumes system window insets once via `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`, preventing double-padding on nested screens and Scaffolds.
3. **Transparent System Bars:** Native edge-to-edge configuration with `android:windowLayoutInDisplayCutoutMode="shortEdges"` and dynamic status/nav icon coloring matching the active theme.
4. **Digit Grouping Formatter:** Display-only digit grouping (Indian `12,34,567.89` and International `1,234,567.89`), keeping internal computation strings pure.
5. **No Dynamic Monet Tinting:** Fixed pure black and tactical orange tokens ensure consistent hardware brand identity.

---

## 📄 License

```
Copyright 2026 MrGokulaKrishnan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
