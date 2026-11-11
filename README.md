# Mathlator

A calculator with 13 converters and 4 finance tools for Android, styled in pure black with an orange gradient accent.

---

## 1. Tech Stack & Architecture

- **Language:** Kotlin 2.1.20 (minSdk 26, targetSdk 35, compileSdk 36)
- **UI:** Jetpack Compose (BOM 2025.05.00) + Material 3, Navigation Compose
- **Architecture:** Clean Architecture + MVVM + StateFlow + Hilt
- **Local Storage:** Room 2.7.1 (KSP) + DataStore Preferences
- **Networking:** Retrofit 2.11.0 + OkHttp + kotlinx.serialization
- **Coroutines:** KotlinX Coroutines Android
- **Splash:** AndroidX Core SplashScreen with custom animated vector

---

## 2. Project Layout

```
com.mathlator.app
├── MainActivity.kt · MathlatorApp.kt (Hilt Application)
├── core/
│   ├── engine/       Tokenizer, Evaluator, Calculator, AngleMode, EvalResult
│   ├── format/       NumberFormatter, DigitGrouping
│   └── finance/      CurrencyCalculator, GstCalculator, EmiCalculator, SipCalculator, LumpSumCalculator, FdCalculator, DiscountCalculator
├── data/
│   ├── db/           AppDatabase, HistoryDao, HistoryEntity, RateDao, RateSnapshotEntity
│   ├── prefs/        SettingsRepository (DataStore Preferences)
│   └── rates/        RateProvider, OpenErApiProvider, FrankfurterProvider, RatesRepository
├── feature/
│   ├── calculator/   CalculatorScreen, CalculatorViewModel, Display, Keypad, SciPanel
│   ├── converters/   ConvertersScreen, UnitConverterScreen, UnitConverterViewModel, ConverterCatalog,
│   │                 UnitPickerSheet, numeral/, date/, age/, bmi/, discount/
│   ├── finance/      FinanceScreen, gst/, currency/, investment/, loan/
│   ├── history/      HistoryScreen, HistoryViewModel
│   └── settings/     SettingsScreen, SettingsViewModel, AboutScreen
└── ui/
    ├── theme/        Color, Type, Shape, Theme
    ├── components/   MathlatorLogo, ResultCard, NumericKeypad, DonutChart, LineChart, GradientUtils
    └── navigation/   MathlatorNavHost, Screen
```

---

## 3. Decisions Log

1. **Pure Kotlin Core Engine:** All calculation engine components in `core/` have no Android dependencies, enabling fast and isolated unit testing.
2. **Recursive Descent Parser:** Clean operator precedence (`^` right-associative, binding tighter than unary minus: `-2^2 = -4`, `2^3^2 = 512`), implicit multiplication (`2(3) = 6`), and auto-closing parentheses for live previews.
3. **Currency Exchange Resilience:** Primary rates provider `open.er-api.com` with zero API key required, automatically falling back to `api.frankfurter.dev`. Rates are cached in Room with a 12-hour TTL and fallback to offline cached data with timestamp display.
4. **Digit Grouping Formatter:** Formatter applies Indian (`12,34,567.89`) and International (`1,234,567.89`) formats purely at the display level; the internal calculation strings remain unformatted decimals.
5. **Brand Gradient Tokens:** Defined in `Color.kt` and `GradientUtils.kt`. No dynamic/Material You tinting to ensure consistent brand identity.
6. **Font Fallback:** System sans-serif with tabular figures (`tnum`) applied, ensuring perfect column alignment across digits.
7. **Window Insets Architecture:** Single-point inset consumption via `WindowInsets.safeDrawing` on the root tab shell `Column`, with `android:windowLayoutInDisplayCutoutMode="shortEdges"` and `isNavigationBarContrastEnforced = false`. Child tool screens never suffer double-padding.
8. **Weighted Calculator Layout:** Weighted heights eliminate empty dead bands between display and keypad across all screen aspect ratios (from 360×640 dp to tall 20:9 devices).

---

## 4. App Shell & Navigation (Section 4)

- **Edge-to-Edge System Insets:** Consumed once at the root shell using `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`. Nested tool screens and Scaffolds do not double-pad.
- **System Bar Icons:** Dynamically adapts based on the active theme: light status/navigation icons on pure black in Dark theme; dark icons in Light theme (`enableEdgeToEdge`).
- **Cutout & Navigation Bar:** Configured with `android:windowLayoutInDisplayCutoutMode="shortEdges"` in `values-v28` and `window.isNavigationBarContrastEnforced = false` on API 29+ to keep 3-button and gesture navigation transparent.
- **Top Bar (56dp):**
  - Left slot: One-handed mode toggle with `contentDescription="One-handed mode"`.
  - Center slot: Three 28dp tab icons with a 44dp orange gradient rounded-square pill behind the active tab icon, vertically centered in the 56dp bar.
  - Right slot: Overflow menu (`⋮`) for History, Settings, About.

---

## 5. Calculator Screen & Keypad (Section 5)

- **Weighted Responsive Layout:**
  - Normal mode: Display `weight(0.9f)` with bottom-aligned result (`contentAlignment = Alignment.BottomCenter`), 1dp divider directly above keypad, Keypad `weight(1.6f)`.
  - Scientific mode: Display `weight(0.6f)`, collapsible Scientific Panel, Keypad `weight(1.6f)`.
  - Keypad rows use equal weights (`1f` each), filling available screen height on all devices with zero dead space.
- **Scientific Mode Toggle:** Bottom-left key displays `f(x)` text and highlights in `Primary` orange when active.
- **Haptics & Gesture Backspace:** Haptic feedback on touch, horizontal drag gesture on display triggers backspace, long-press opens Copy/Share dialog.

---

## 6. Currency Converter (Section 8.2)

- **Three-Currency Stack (1 Main + 2 Converted):**
  - **Card 1 (MAIN):** Active input with 1.5dp brand-gradient outline, editable amount (36sp / 32sp on compact screens), and currency chip.
  - **Card 2 (CONVERTED):** Read-only converted amount, currency chip, live rate caption (`1 INR = 0.0111 USD`), and 40dp swap/make-main button.
  - **Card 3 (CONVERTED):** Second converted currency with rate caption and swap/make-main button.
- **Make Main & Slot Swapping:** Tapping the swap button or card body promotes that converted card to MAIN. If a chosen currency in the picker is already active, the two slots swap instead of creating a duplicate.
- **Clean 4-Column Keypad (Bug 3):**
  ```
  7   8   9   ⌫ (tall, rows 1–2)
  4   5   6   ⌫
  1   2   3   C (tall, rows 3–4)
  .   0   00  C
  ```
  Zero empty cells. Keypad is bottom-anchored, followed by the attribution line (`Rates from open.er-api.com`).

---

## 7. Settings & Persistence (Section 9)

- **Default Currencies:** Three persistent slots (`currency_from`, `currency_to`, `currency_out2`) stored in DataStore.
  - Default region detection: INR for India (`INR, USD, EUR`), region-based fallback (`USD, EUR, GBP` for US).
  - Settings → Data → "Default currencies" displays and edits all three defaults via dialog.
- **Settings Available:** Theme (Dark, Light, System), Haptic feedback, Key sound, Angle mode (DEG/RAD), Decimal precision (2–15), Digit grouping (Indian, International, None), Keep screen on, One-handed side (Left/Right), Clear history, Clear cached rates.

---

## 8. Build & Run Instructions

### Debug APK Build & Automated Stamping
```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-apk.ps1
```
Or on macOS/Linux:
```bash
./scripts/build-apk.sh
```

### Verification (Tests & Lint)
```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify.ps1
```

### Local Wi-Fi Device Download Server
```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-apk.ps1 -Serve
```

---

## 9. QA Checklist

- [x] **Status Bar Insets (Bug 1):** Tab bar icons sit below status-bar clock/battery on gesture nav, 3-button nav, and punch-hole displays without overlap.
- [x] **Calculator Dead Space (Bug 2):** Display is bottom-aligned above divider; keypad fills remaining height evenly on both small and tall phones.
- [x] **Converter & Currency Keypad (Bug 3):** 4-column grid with tall `⌫` (rows 1–2) and tall `C` (rows 3–4); zero empty cells; attribution at bottom.
- [x] **Currency Three-Card Layout:** Card 1 (MAIN, gradient outline), Cards 2 & 3 (CONVERTED with rate captions and 40dp make-main buttons).
- [x] **Currency Make Main & Swapping:** Promotes converted currency to main with current value; selecting an active currency swaps slots.
- [x] **Currency Persistence:** DataStore saves and restores `main`, `out1`, `out2`; default region currencies logic.
- [x] **Scientific Toggle:** `f(x)` key highlights in Primary orange when active.
- [x] **One-Handed Mode:** Content description "One-handed mode" on top-left icon.
