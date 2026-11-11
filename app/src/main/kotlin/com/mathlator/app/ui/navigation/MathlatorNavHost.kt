package com.mathlator.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.StayCurrentPortrait
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mathlator.app.data.prefs.SettingsRepository
import com.mathlator.app.feature.calculator.CalculatorScreen
import com.mathlator.app.feature.calculator.CalculatorViewModel
import com.mathlator.app.feature.converters.ConvertersScreen
import com.mathlator.app.feature.converters.UnitConverterScreen
import com.mathlator.app.feature.converters.UnitConverterViewModel
import com.mathlator.app.feature.converters.age.AgeScreen
import com.mathlator.app.feature.converters.age.AgeViewModel
import com.mathlator.app.feature.converters.bmi.BmiScreen
import com.mathlator.app.feature.converters.bmi.BmiViewModel
import com.mathlator.app.feature.converters.date.DateScreen
import com.mathlator.app.feature.converters.date.DateViewModel
import com.mathlator.app.feature.converters.discount.DiscountScreen
import com.mathlator.app.feature.converters.discount.DiscountViewModel
import com.mathlator.app.feature.converters.numeral.NumeralScreen
import com.mathlator.app.feature.converters.numeral.NumeralViewModel
import com.mathlator.app.feature.finance.FinanceScreen
import com.mathlator.app.feature.finance.currency.CurrencyScreen
import com.mathlator.app.feature.finance.currency.CurrencyViewModel
import com.mathlator.app.feature.finance.gst.GstScreen
import com.mathlator.app.feature.finance.gst.GstViewModel
import com.mathlator.app.feature.finance.investment.InvestmentScreen
import com.mathlator.app.feature.finance.investment.InvestmentViewModel
import com.mathlator.app.feature.finance.loan.AmortizationScreen
import com.mathlator.app.feature.finance.loan.AmortizationViewModel
import com.mathlator.app.feature.finance.loan.LoanScreen
import com.mathlator.app.feature.finance.loan.LoanViewModel
import com.mathlator.app.feature.history.HistoryScreen
import com.mathlator.app.feature.history.HistoryViewModel
import com.mathlator.app.feature.settings.AboutScreen
import com.mathlator.app.feature.settings.SettingsScreen
import com.mathlator.app.feature.settings.SettingsViewModel
import com.mathlator.app.ui.components.brandGradientBrush
import com.mathlator.app.ui.theme.Primary
import com.mathlator.app.ui.theme.mathlatorColors
import kotlinx.coroutines.launch

@Composable
fun MathlatorNavHost(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    calculatorViewModel: CalculatorViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main_tabs",
        modifier = modifier,
        enterTransition = { slideInHorizontally(tween(250)) { 72 } + fadeIn(tween(250)) },
        exitTransition = { slideOutHorizontally(tween(250)) { -72 } + fadeOut(tween(250)) },
        popEnterTransition = { slideInHorizontally(tween(250)) { -72 } + fadeIn(tween(250)) },
        popExitTransition = { slideOutHorizontally(tween(250)) { 72 } + fadeOut(tween(250)) },
    ) {
        composable("main_tabs") {
            MainTabShell(
                calculatorViewModel = calculatorViewModel,
                settingsRepository = settingsRepository,
                onNavigateToRoute = { route -> navController.navigate(route) },
            )
        }

        // Converters detail routes
        composable(
            route = Screen.UnitConverter.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
        ) {
            val vm: UnitConverterViewModel = hiltViewModel()
            UnitConverterScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.NumeralConverter.route) {
            val vm: NumeralViewModel = hiltViewModel()
            NumeralScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.DateConverter.route) {
            val vm: DateViewModel = hiltViewModel()
            DateScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.AgeConverter.route) {
            val vm: AgeViewModel = hiltViewModel()
            AgeScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.BmiConverter.route) {
            val vm: BmiViewModel = hiltViewModel()
            BmiScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.DiscountConverter.route) {
            val vm: DiscountViewModel = hiltViewModel()
            DiscountScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // Finance detail routes
        composable(Screen.GstTool.route) {
            val vm: GstViewModel = hiltViewModel()
            GstScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.CurrencyTool.route) {
            val vm: CurrencyViewModel = hiltViewModel()
            CurrencyScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.InvestmentTool.route) {
            val vm: InvestmentViewModel = hiltViewModel()
            InvestmentScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.LoanTool.route) {
            val vm: LoanViewModel = hiltViewModel()
            LoanScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onViewAmortization = { amount, rate, months ->
                    navController.navigate(Screen.Amortization.route(amount, rate, months))
                },
            )
        }
        composable(
            route = Screen.Amortization.route,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("rate") { type = NavType.StringType },
                navArgument("months") { type = NavType.IntType },
            ),
        ) {
            val vm: AmortizationViewModel = hiltViewModel()
            AmortizationScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // History, Settings, About
        composable(Screen.History.route) {
            val vm: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onSelectExpression = { expr ->
                    calculatorViewModel.loadExpression(expr)
                },
            )
        }
        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainTabShell(
    calculatorViewModel: CalculatorViewModel,
    settingsRepository: SettingsRepository,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = mathlatorColors
    val scope = rememberCoroutineScope()
    var savedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = savedTabIndex, pageCount = { 3 })
    var showOverflowMenu by remember { mutableStateOf(false) }

    val isOneHanded by settingsRepository.oneHandedEnabled.collectAsState(initial = false)

    // Single inset consumption: status bar + nav bar + cutout + IME.
    // Nested Scaffold/TopAppBar screens do NOT receive these insets again.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // ── Top Bar (56dp) ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.background)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left slot: one-handed toggle (only on Calculator tab)
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (pagerState.currentPage == 0) {
                    IconButton(
                        onClick = {
                            scope.launch { settingsRepository.setOneHanded(!isOneHanded) }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "One-handed mode"
                        },
                    ) {
                        Icon(
                            imageVector = if (isOneHanded) Icons.Outlined.OpenInFull else Icons.Outlined.StayCurrentPortrait,
                            contentDescription = "One-handed mode",
                            tint = if (isOneHanded) Primary else colors.textDim,
                        )
                    }
                }
            }

            // Centre: three tab icons, evenly spaced
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabIconItem(
                    icon = Icons.Outlined.Calculate,
                    contentDescription = "Calculator",
                    isSelected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                )
                Spacer(Modifier.padding(horizontal = 16.dp))
                TabIconItem(
                    icon = Icons.Outlined.GridView,
                    contentDescription = "Converters",
                    isSelected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                )
                Spacer(Modifier.padding(horizontal = 16.dp))
                TabIconItem(
                    icon = Icons.Outlined.CurrencyExchange,
                    contentDescription = "Finance",
                    isSelected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                )
            }

            // Right: overflow menu
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = { showOverflowMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More options",
                        tint = colors.textDim,
                    )
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    modifier = Modifier
                        .background(colors.surfaceHigh, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    DropdownMenuItem(
                        text = { Text("History", color = colors.textMain) },
                        onClick = { showOverflowMenu = false; onNavigateToRoute(Screen.History.route) },
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = colors.textMain) },
                        onClick = { showOverflowMenu = false; onNavigateToRoute(Screen.Settings.route) },
                    )
                    DropdownMenuItem(
                        text = { Text("About", color = colors.textMain) },
                        onClick = { showOverflowMenu = false; onNavigateToRoute(Screen.About.route) },
                    )
                }
            }
        }

        // ── Pager ─────────────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> CalculatorScreen(
                    viewModel = calculatorViewModel,
                    settingsRepository = settingsRepository,
                )
                1 -> ConvertersScreen(
                    onNavigateToRoute = onNavigateToRoute,
                )
                2 -> FinanceScreen(
                    onNavigateToRoute = onNavigateToRoute,
                )
            }
        }
    }
}

@Composable
private fun TabIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = mathlatorColors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.background(brandGradientBrush(), RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 22.dp),
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else colors.textDim,
            modifier = Modifier.size(28.dp),
        )
    }
}
