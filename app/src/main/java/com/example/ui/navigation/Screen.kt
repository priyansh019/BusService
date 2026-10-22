package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Plan : Screen("plan", "Plan", Icons.Default.Search)
    object Routes : Screen("routes", "Routes", Icons.Default.DirectionsBus)
    object Saved : Screen("saved", "Saved", Icons.Default.Bookmark)
    object Contribute : Screen("contribute", "Contribute", Icons.Default.Edit)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    object JourneyDetail : Screen("journey_detail/{journeyId}", "Journey Detail") {
        fun createRoute(journeyId: String) = "journey_detail/$journeyId"
    }

    object RouteDetail : Screen("route_detail/{routeId}", "Route Detail") {
        fun createRoute(routeId: String) = "route_detail/$routeId"
    }

    object StopDetail : Screen("stop_detail/{stopId}", "Stop Detail") {
        fun createRoute(stopId: String) = "stop_detail/$stopId"
    }

    object FareCalculator : Screen("fare_calculator", "Fare & Savings Calculator")
    object Alerts : Screen("alerts", "Service Alerts")
    object Moderator : Screen("moderator", "Moderation Queue")
    object Operator : Screen("operator", "Operator Console")
    object Driver : Screen("driver", "Driver Console")
    object Admin : Screen("admin", "Administrator Console")
    object SelfTest : Screen("selftest", "Self-Test Suite")
    object DeveloperJson : Screen("developer_json", "Developer JSON Inspector")
    object Onboarding : Screen("onboarding", "Welcome to Bus Discovery")

    companion object {
        val bottomNavItems = listOf(Plan, Routes, Saved, Contribute, Profile)
    }
}
