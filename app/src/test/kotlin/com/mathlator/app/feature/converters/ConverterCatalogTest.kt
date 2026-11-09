package com.mathlator.app.feature.converters

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext

/** Unit tests for [ConverterCatalog] (Section 13). */
class ConverterCatalogTest {

    private val MC = MathContext.DECIMAL128

    private fun roundTrip(value: BigDecimal, units: List<UnitDef>, delta: Double = 1e-9) {
        for (from in units) {
            for (to in units) {
                if (from.id == to.id) continue
                val converted = ConverterCatalog.convert(value, from, to)
                val back = ConverterCatalog.convert(converted, to, from)
                assertEquals(
                    "Round-trip ${from.id}→${to.id}→${from.id}",
                    value.toDouble(), back.toDouble(), delta,
                )
            }
        }
    }

    @Test fun `Length round trip`() = roundTrip(BigDecimal("1"), ConverterCatalog.LENGTH)
    @Test fun `Area round trip`()   = roundTrip(BigDecimal("1"), ConverterCatalog.AREA)
    @Test fun `Volume round trip`() = roundTrip(BigDecimal("1"), ConverterCatalog.VOLUME)
    @Test fun `Mass round trip`()   = roundTrip(BigDecimal("1"), ConverterCatalog.MASS)
    @Test fun `Speed round trip`()  = roundTrip(BigDecimal("1"), ConverterCatalog.SPEED)
    @Test fun `Time round trip`()   = roundTrip(BigDecimal("1"), ConverterCatalog.TIME)
    @Test fun `Data decimal round trip`() = roundTrip(BigDecimal("1"), ConverterCatalog.DATA_DECIMAL)
    @Test fun `Data binary round trip`()  = roundTrip(BigDecimal("1"), ConverterCatalog.DATA_BINARY)

    @Test fun `1 mi equals 1_609344 km`() {
        val mi = ConverterCatalog.LENGTH.first { it.id == "mi" }
        val km = ConverterCatalog.LENGTH.first { it.id == "km" }
        val result = ConverterCatalog.convert(BigDecimal("1"), mi, km)
        assertEquals(1.609344, result.toDouble(), 1e-6)
    }

    @Test fun `100 Celsius equals 212 Fahrenheit`() {
        val r = ConverterCatalog.convertTemperature(BigDecimal("100"), TempUnit.Celsius, TempUnit.Fahrenheit)
        assertEquals(212.0, r.toDouble(), 1e-9)
    }

    @Test fun `0 Celsius equals 273_15 Kelvin`() {
        val r = ConverterCatalog.convertTemperature(BigDecimal("0"), TempUnit.Celsius, TempUnit.Kelvin)
        assertEquals(273.15, r.toDouble(), 1e-9)
    }

    @Test fun `255 DEC to HEX is FF`() {
        val n = BigDecimal("255").toBigInteger()
        assertEquals("FF", n.toString(16).uppercase())
    }

    @Test fun `255 DEC to BIN is 11111111`() {
        val n = BigDecimal("255").toBigInteger()
        assertEquals("11111111", n.toString(2))
    }

    @Test fun `1 GB decimal equals 1000 MB`() {
        val gb = ConverterCatalog.DATA_DECIMAL.first { it.id == "GB" }
        val mb = ConverterCatalog.DATA_DECIMAL.first { it.id == "MB" }
        val r = ConverterCatalog.convert(BigDecimal("1"), gb, mb)
        assertEquals(1000.0, r.toDouble(), 1e-9)
    }

    @Test fun `1 GiB binary equals 1024 MiB`() {
        val gib = ConverterCatalog.DATA_BINARY.first { it.id == "GiB" }
        val mib = ConverterCatalog.DATA_BINARY.first { it.id == "MiB" }
        val r = ConverterCatalog.convert(BigDecimal("1"), gib, mib)
        assertEquals(1024.0, r.toDouble(), 1e-9)
    }

    @Test fun `Temperature round trip C to F`() {
        val original = BigDecimal("37")
        val f = ConverterCatalog.convertTemperature(original, TempUnit.Celsius, TempUnit.Fahrenheit)
        val back = ConverterCatalog.convertTemperature(f, TempUnit.Fahrenheit, TempUnit.Celsius)
        assertEquals(original.toDouble(), back.toDouble(), 1e-9)
    }
}
