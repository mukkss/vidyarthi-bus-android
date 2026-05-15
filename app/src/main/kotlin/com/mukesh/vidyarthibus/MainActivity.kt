package com.mukesh.vidyarthibus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mukesh.vidyarthibus.ui.alternatives.AlternativesScreen
import com.mukesh.vidyarthibus.ui.navigation.Screen
import com.mukesh.vidyarthibus.ui.onboarding.OnboardingScreen
import com.mukesh.vidyarthibus.ui.routes.RouteSelectionScreen
import com.mukesh.vidyarthibus.ui.splash.SplashScreen
import com.mukesh.vidyarthibus.ui.status.LiveStatusScreen
import com.mukesh.vidyarthibus.ui.theme.VidyarthiBusTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VidyarthiBusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(onNavigateToRouteSelection = {
                                navController.navigate(Screen.RouteSelection.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.RouteSelection.route) {
                            RouteSelectionScreen(onRouteSelected = { routeId ->
                                navController.navigate(Screen.LiveStatus.createRoute(routeId))
                            })
                        }
                        composable(Screen.LiveStatus.route) {
                            LiveStatusScreen(
                                onNavigateToAlternatives = { routeId ->
                                    navController.navigate(Screen.Alternatives.createRoute(routeId))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Alternatives.route) {
                            AlternativesScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
