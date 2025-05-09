package com.zimneos.scan2contact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.zimneos.scan2contact.ui.screens.ContactScreen
import com.zimneos.scan2contact.ui.Details
import com.zimneos.scan2contact.ui.Home
import com.zimneos.scan2contact.ui.screens.MainScreen
import com.zimneos.scan2contact.ui.Recent
import com.zimneos.scan2contact.ui.screens.MyScansScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                enableEdgeToEdge()
                NavigationSetup()
            }
        }
    }
}


@Composable
fun NavigationSetup() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Home) {
        composable<Home>(
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            }
        ) {
            MainScreen(
                navigateToContactScreen = { navController.navigate(Details(it.toString())) },
                navigateToRecentScreen = { navController.navigate(Recent) })
        }
        composable<Recent>(
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            }
        ) {
            MyScansScreen(
                navigateToContactScreen = { navController.navigate(Details(it.toString())) },
                navigateToBackScreen = { navController.popBackStack() }
            )
        }

        composable<Details>(
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            }
        ) { backStackEntry ->
            ContactScreen(
                scammedImageUri = backStackEntry.toRoute<Details>().scannedImageUri.toUri(),
            )
        }
    }
}
