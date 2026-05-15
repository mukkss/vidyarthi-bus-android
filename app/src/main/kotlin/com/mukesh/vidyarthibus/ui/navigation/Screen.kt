package com.mukesh.vidyarthibus.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object RouteSelection : Screen("route_selection")
    object LiveStatus : Screen("live_status/{routeId}") {
        fun createRoute(routeId: String) = "live_status/$routeId"
    }
    object Alternatives : Screen("alternatives/{routeId}") {
        fun createRoute(routeId: String) = "alternatives/$routeId"
    }
}
