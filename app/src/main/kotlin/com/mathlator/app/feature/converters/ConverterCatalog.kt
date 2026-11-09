package com.mathlator.app.feature.converters

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext.DECIMAL128

/** A single unit definition with a conversion factor to the base unit. */
data class UnitDef(
    val id: String,
    val symbol: String,
    val nameResKey: String,     // string resource key (or display name fallback)
    val factor: BigDecimal,     // 1 [unit] = [factor] [base]
    val isPinned: Boolean = false, // shown in "Common" section of picker
)

/** A temperature unit — uses formula conversion instead of a factor. */
enum class TempUnit(val symbol: String) { Celsius("°C"), Fahrenheit("°F"), Kelvin("K") }

/** Category of converters. */
enum class ConverterCategory(val id: String) {
    Length("length"),
    Area("area"),
    Volume("volume"),
    Mass("mass"),
    Speed("speed"),
    Time("time"),
    Temperature("temperature"),
    Data("data"),
}

/**
 * Data-driven catalog of unit converter definitions.
 * All factors are exact [BigDecimal] values relative to the base unit of each category.
 *
 * Base units:
 * - Length: metre (m)
 * - Area: square metre (m²)
 * - Volume: litre (L)
 * - Mass: kilogram (kg)
 * - Speed: metre per second (m/s)
 * - Time: second (s)
 * - Data: byte (B)  — with separate decimal/binary flag
 */
object ConverterCatalog {

    // ── Length (base: m) ───────────────────────────────────────────────────────
    val LENGTH = listOf(
        UnitDef("mm",  "mm",  "Millimetre",   bd("0.001"),          isPinned = false),
        UnitDef("cm",  "cm",  "Centimetre",   bd("0.01"),           isPinned = true),
        UnitDef("m",   "m",   "Metre",        bd("1"),              isPinned = true),
        UnitDef("km",  "km",  "Kilometre",    bd("1000"),           isPinned = true),
        UnitDef("in",  "in",  "Inch",         bd("0.0254"),         isPinned = true),
        UnitDef("ft",  "ft",  "Foot",         bd("0.3048"),         isPinned = true),
        UnitDef("yd",  "yd",  "Yard",         bd("0.9144"),         isPinned = false),
        UnitDef("mi",  "mi",  "Mile",         bd("1609.344"),       isPinned = true),
        UnitDef("nmi", "nmi", "Nautical mile",bd("1852"),           isPinned = false),
    )

    // ── Area (base: m²) ────────────────────────────────────────────────────────
    val AREA = listOf(
        UnitDef("mm2",    "mm²",   "Square millimetre", bd("0.000001"),          isPinned = false),
        UnitDef("cm2",    "cm²",   "Square centimetre", bd("0.0001"),            isPinned = false),
        UnitDef("m2",     "m²",    "Square metre",      bd("1"),                 isPinned = true),
        UnitDef("km2",    "km²",   "Square kilometre",  bd("1000000"),           isPinned = true),
        UnitDef("ha",     "ha",    "Hectare",           bd("10000"),             isPinned = true),
        UnitDef("acre",   "acre",  "Acre",              bd("4046.8564224"),      isPinned = true),
        UnitDef("in2",    "in²",   "Square inch",       bd("0.00064516"),        isPinned = false),
        UnitDef("ft2",    "ft²",   "Square foot",       bd("0.09290304"),        isPinned = true),
        UnitDef("yd2",    "yd²",   "Square yard",       bd("0.83612736"),        isPinned = false),
        UnitDef("mi2",    "mi²",   "Square mile",       bd("2589988.110336"),    isPinned = false),
        UnitDef("cent",   "cent",  "Cent",              bd("40.468564224"),      isPinned = true),
        UnitDef("ground", "ground","Ground",            bd("222.967296"),        isPinned = false),
    )

    // ── Volume (base: L) ───────────────────────────────────────────────────────
    val VOLUME = listOf(
        UnitDef("mL",      "mL",      "Millilitre",     bd("0.001"),              isPinned = true),
        UnitDef("L",       "L",       "Litre",          bd("1"),                  isPinned = true),
        UnitDef("m3",      "m³",      "Cubic metre",    bd("1000"),               isPinned = true),
        UnitDef("tsp_us",  "tsp(US)", "US Teaspoon",    bd("0.00492892159375"),   isPinned = false),
        UnitDef("tbsp_us", "tbsp(US)","US Tablespoon",  bd("0.01478676478125"),   isPinned = false),
        UnitDef("floz_us", "fl oz",   "US Fluid ounce", bd("0.0295735295625"),    isPinned = false),
        UnitDef("cup_us",  "cup",     "US Cup",         bd("0.2365882365"),       isPinned = false),
        UnitDef("pt_us",   "pt(US)",  "US Pint",        bd("0.473176473"),        isPinned = false),
        UnitDef("qt_us",   "qt(US)",  "US Quart",       bd("0.946352946"),        isPinned = false),
        UnitDef("gal_us",  "gal(US)", "US Gallon",      bd("3.785411784"),        isPinned = true),
        UnitDef("gal_uk",  "gal(UK)", "UK Gallon",      bd("4.54609"),            isPinned = false),
    )

    // ── Mass (base: kg) ────────────────────────────────────────────────────────
    val MASS = listOf(
        UnitDef("mg",      "mg",      "Milligram",   bd("0.000001"),        isPinned = false),
        UnitDef("g",       "g",       "Gram",        bd("0.001"),           isPinned = true),
        UnitDef("kg",      "kg",      "Kilogram",    bd("1"),               isPinned = true),
        UnitDef("quintal", "q",       "Quintal",     bd("100"),             isPinned = false),
        UnitDef("tonne",   "t",       "Metric tonne",bd("1000"),            isPinned = true),
        UnitDef("oz",      "oz",      "Ounce",       bd("0.028349523125"),  isPinned = true),
        UnitDef("lb",      "lb",      "Pound",       bd("0.45359237"),      isPinned = true),
        UnitDef("stone",   "st",      "Stone",       bd("6.35029318"),      isPinned = false),
    )

    // ── Speed (base: m/s) ─────────────────────────────────────────────────────
    val SPEED = listOf(
        UnitDef("ms",    "m/s",   "Metre/second",  bd("1"),                   isPinned = true),
        UnitDef("kmh",   "km/h",  "Kilometre/hour",bd("1").divide(bd("3.6"), MC), isPinned = true),
        UnitDef("mph",   "mph",   "Mile/hour",     bd("0.44704"),             isPinned = true),
        UnitDef("knot",  "kn",    "Knot",          bd("1852").divide(bd("3600"), MC), isPinned = false),
        UnitDef("fts",   "ft/s",  "Foot/second",   bd("0.3048"),              isPinned = false),
    )

    // ── Time (base: s) ────────────────────────────────────────────────────────
    val TIME = listOf(
        UnitDef("ms",     "ms",    "Millisecond", bd("0.001"),    isPinned = false),
        UnitDef("s",      "s",     "Second",      bd("1"),        isPinned = true),
        UnitDef("min",    "min",   "Minute",      bd("60"),       isPinned = true),
        UnitDef("h",      "h",     "Hour",        bd("3600"),     isPinned = true),
        UnitDef("day",    "day",   "Day",         bd("86400"),    isPinned = true),
        UnitDef("week",   "week",  "Week",        bd("604800"),   isPinned = true),
        UnitDef("month",  "month", "Month",       bd("2629800"),  isPinned = false),
        UnitDef("year",   "year",  "Year",        bd("31557600"), isPinned = true),
    )

    // ── Data (base: byte — decimal) ───────────────────────────────────────────
    // Decimal (SI) units: 1 KB = 1000 B
    val DATA_DECIMAL = listOf(
        UnitDef("bit",  "bit",  "Bit",      bd("0.125"),                   isPinned = false),
        UnitDef("B",    "B",    "Byte",     bd("1"),                       isPinned = true),
        UnitDef("KB",   "KB",   "Kilobyte", bd("1000"),                    isPinned = true),
        UnitDef("MB",   "MB",   "Megabyte", bd("1000000"),                 isPinned = true),
        UnitDef("GB",   "GB",   "Gigabyte", bd("1000000000"),              isPinned = true),
        UnitDef("TB",   "TB",   "Terabyte", bd("1000000000000"),           isPinned = true),
        UnitDef("PB",   "PB",   "Petabyte", bd("1000000000000000"),        isPinned = false),
    )

    // Binary (IEC) units: 1 KiB = 1024 B
    val DATA_BINARY = listOf(
        UnitDef("bit",  "bit",  "Bit",      bd("0.125"),                   isPinned = false),
        UnitDef("B",    "B",    "Byte",     bd("1"),                       isPinned = true),
        UnitDef("KiB",  "KiB",  "Kibibyte", bd("1024"),                    isPinned = true),
        UnitDef("MiB",  "MiB",  "Mebibyte", bd("1048576"),                 isPinned = true),
        UnitDef("GiB",  "GiB",  "Gibibyte", bd("1073741824"),              isPinned = true),
        UnitDef("TiB",  "TiB",  "Tebibyte", bd("1099511627776"),           isPinned = true),
        UnitDef("PiB",  "PiB",  "Pebibyte", bd("1125899906842624"),        isPinned = false),
    )

    /** Convert [value] from [fromUnit] to [toUnit] within the same category. */
    fun convert(value: BigDecimal, fromUnit: UnitDef, toUnit: UnitDef): BigDecimal {
        if (fromUnit.id == toUnit.id) return value
        val baseValue = value.multiply(fromUnit.factor, MC)
        return baseValue.divide(toUnit.factor, MC)
    }

    /** Convert temperature between [from] and [to] units. */
    fun convertTemperature(value: BigDecimal, from: TempUnit, to: TempUnit): BigDecimal {
        if (from == to) return value
        val celsius: BigDecimal = when (from) {
            TempUnit.Celsius    -> value
            TempUnit.Fahrenheit -> (value - bd("32")).multiply(bd("5"), MC).divide(bd("9"), MC)
            TempUnit.Kelvin     -> value - bd("273.15")
        }
        return when (to) {
            TempUnit.Celsius    -> celsius
            TempUnit.Fahrenheit -> celsius.multiply(bd("9"), MC).divide(bd("5"), MC) + bd("32")
            TempUnit.Kelvin     -> celsius + bd("273.15")
        }
    }

    private fun bd(s: String) = BigDecimal(s)
}

private operator fun BigDecimal.minus(other: BigDecimal) = this.subtract(other, MC)
private operator fun BigDecimal.plus(other: BigDecimal)  = this.add(other, MC)
