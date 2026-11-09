package com.mathlator.app.ui.navigation

/** All navigation destinations in the app. */
sealed class Screen(val route: String) {
    object Calculator   : Screen("calculator")
    object Converters   : Screen("converters")
    object Finance      : Screen("finance")
    object History      : Screen("history")
    object Settings     : Screen("settings")
    object About        : Screen("about")

    // Converter detail screens
    object UnitConverter : Screen("unit_converter/{category}") {
        fun route(category: String) = "unit_converter/$category"
    }
    object NumeralConverter  : Screen("numeral_converter")
    object DateConverter     : Screen("date_converter")
    object AgeConverter      : Screen("age_converter")
    object BmiConverter      : Screen("bmi_converter")
    object DiscountConverter : Screen("discount_converter")

    // Finance detail screens
    object GstTool        : Screen("gst_tool")
    object CurrencyTool   : Screen("currency_tool")
    object InvestmentTool : Screen("investment_tool")
    object LoanTool       : Screen("loan_tool")
    object Amortization   : Screen("amortization/{amount}/{rate}/{months}") {
        fun route(amount: String, rate: String, months: Int) =
            "amortization/$amount/$rate/$months"
    }
}
