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
│   └── finance/      GstCalculator, EmiCalculator, SipCalculator, LumpSumCalculator, FdCalculator, DiscountCalculator
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

---

## 4. How to Edit GST Rates

GST slabs are set by the GST Council and can change. Slabs are loaded dynamically from:

```
app/src/main/assets/gst_rates.json
```

Default content:
```json
[0, 3, 5, 18, 40]
```

> **Note:** Verify and update `gst_rates.json` before every production release to reflect the latest council tax slabs without modifying Kotlin code.

---

## 5. Build & Run Instructions

### Debug APK
```bash
./gradlew assembleDebug
```

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Release App Bundle (AAB)
```bash
./gradlew bundleRelease
```

### Release Signing
Release builds use R8 code shrinking and resource minification. Configure signing in `local.properties` or environment variables:
```properties
KEYSTORE_PATH=/path/to/release.keystore
KEYSTORE_PASS=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASS=your_key_password
```
*Never commit keystores or release credentials to version control.*

---

## 6. Manual QA Checklist

- [x] **Calculator:** `259 - 449 = -190` live preview and commit.
- [x] **Percent operations:** `200 + 10% = 220`, `200 - 10% = 180`, `200 × 10% = 20`, `200 ÷ 50% = 400`.
- [x] **Parentheses & Order:** `2 + 3 × 4 = 14`, `(2 + 3) × 4 = 20`, `2^3^2 = 512`, `-2^2 = -4`.
- [x] **Trigonometry & Functions:** `sin(30°) = 0.5`, `cos(90°) = 0`, `ln(e) = 1`, `log(1000) = 3`, `10! = 3628800`.
- [x] **Scientific Panel:** Slides in/out with 250ms animation; `2nd` toggles inverse functions.
- [x] **Unit Converters:** Length, Area, Volume, Mass, Speed, Time, Temperature, Data. Bidirectional input, swap button 180° rotation, all-units list.
- [x] **Special Converters:**
  - Numeral system: BIN/OCT/DEC/HEX with active keypad filters.
  - Date: Difference & Add/Subtract tabs with DatePicker.
  - Age: DOB, total units, next birthday calculation with leap year handling.
  - BMI: Metric/Imperial, Asian cut-offs, colored segmented bar.
  - Discount: Forward and reverse calculations with extra discount and tax.
- [x] **Finance Tools:**
  - GST: Add/Remove, dynamic rates from `assets/gst_rates.json`, CGST/SGST/IGST split.
  - Currency: Exchange rate calculation, 12h Room cache, offline timestamp.
  - Investment: SIP (with step-up), Lump sum, FD (with compounding), Canvas LineChart.
  - Loan: Monthly EMI, DonutChart, Amortization schedule screen with CSV sharing.
- [x] **History & Settings:** Room persistence, grouped by date, swipe-to-delete with undo, theme switching, precision slider, one-handed keypad toggle.
