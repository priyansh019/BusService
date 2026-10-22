package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.admin.AdminScreen
import com.example.ui.alerts.AlertsScreen
import com.example.ui.contribute.ContributeScreen
import com.example.ui.driver.DriverScreen
import com.example.ui.moderator.ModeratorScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.operator.OperatorScreen
import com.example.ui.plan.FareCalculatorContent
import com.example.ui.plan.JourneyDetailScreen
import com.example.ui.plan.PlanScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.routes.RouteDetailScreen
import com.example.ui.routes.RoutesScreen
import com.example.ui.saved.SavedJourneysScreen
import com.example.ui.selftest.DeveloperJsonScreen
import com.example.ui.selftest.SelfTestScreen
import com.example.ui.tour.GuidedDemoTourOverlay
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val settings by container.repository.getAppSettingsFlow().collectAsState(initial = null)

    var showTourOverlay by remember { mutableStateOf(false) }
    var isOnboardingActive by remember { mutableStateOf(false) }

    // Check if onboarding completed
    androidx.compose.runtime.LaunchedEffect(settings) {
        if (settings != null && !settings!!.hasCompletedOnboarding) {
            isOnboardingActive = true
        }
    }

    if (isOnboardingActive) {
        OnboardingScreen(
            onComplete = {
                isOnboardingActive = false
                settings?.let { s ->
                    coroutineScope.launch {
                        container.repository.updateAppSettings(s.copy(hasCompletedOnboarding = true))
                    }
                }
            }
        )
        return
    }

    val isBottomBarVisible = Screen.bottomNavItems.any { it.route == currentRoute }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TransitNavy
                    ) {
                        Screen.bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon ?: return@NavigationBarItem,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TransitNavy,
                                    selectedTextColor = TransitNavy,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Plan.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Plan.route) {
                    PlanScreen(
                        container = container,
                        onNavigateToJourney = { journeyId ->
                            navController.navigate(Screen.JourneyDetail.createRoute(journeyId))
                        },
                        onNavigateToFareCalculator = { busFare, altFare ->
                            navController.navigate(Screen.FareCalculator.route)
                        },
                        onNavigateToAlerts = {
                            navController.navigate(Screen.Alerts.route)
                        }
                    )
                }

                composable(Screen.Routes.route) {
                    RoutesScreen(
                        container = container,
                        onNavigateToRoute = { routeId ->
                            navController.navigate(Screen.RouteDetail.createRoute(routeId))
                        }
                    )
                }

                composable(Screen.Saved.route) {
                    SavedJourneysScreen(
                        container = container,
                        onNavigateToPlan = {
                            navController.navigate(Screen.Plan.route)
                        }
                    )
                }

                composable(Screen.Contribute.route) {
                    ContributeScreen(container = container)
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        container = container,
                        onNavigateToModerator = { navController.navigate(Screen.Moderator.route) },
                        onNavigateToOperator = { navController.navigate(Screen.Operator.route) },
                        onNavigateToDriver = { navController.navigate(Screen.Driver.route) },
                        onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                        onNavigateToSelfTest = { navController.navigate(Screen.SelfTest.route) },
                        onNavigateToDevJson = { navController.navigate(Screen.DeveloperJson.route) },
                        onStartTour = { showTourOverlay = true }
                    )
                }

                composable(
                    route = Screen.JourneyDetail.route,
                    arguments = listOf(navArgument("journeyId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val journeyId = backStackEntry.arguments?.getString("journeyId") ?: ""
                    JourneyDetailScreen(
                        journeyId = journeyId,
                        container = container,
                        onBack = { navController.popBackStack() },
                        onReportIssue = {
                            navController.navigate(Screen.Contribute.route)
                        }
                    )
                }

                composable(
                    route = Screen.RouteDetail.route,
                    arguments = listOf(navArgument("routeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                    RouteDetailScreen(
                        routeId = routeId,
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.FareCalculator.route) {
                    Scaffold { p ->
                        Box(modifier = Modifier.padding(p).padding(16.dp)) {
                            FareCalculatorContent()
                        }
                    }
                }

                composable(Screen.Alerts.route) {
                    AlertsScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Moderator.route) {
                    ModeratorScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Operator.route) {
                    OperatorScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Driver.route) {
                    DriverScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Admin.route) {
                    AdminScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.SelfTest.route) {
                    SelfTestScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.DeveloperJson.route) {
                    DeveloperJsonScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Tour Overlay
        if (showTourOverlay) {
            GuidedDemoTourOverlay(onDismiss = { showTourOverlay = false })
        }
    }
}
