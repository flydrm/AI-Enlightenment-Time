package com.enlightenment.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.enlightenment.presentation.ui.screens.home.HomeScreen
import com.enlightenment.presentation.ui.screens.story.StoryScreen
import com.enlightenment.presentation.camera.CameraScreen as NewCameraScreen
import com.enlightenment.presentation.voice.VoiceScreen
import com.enlightenment.presentation.story.player.StoryPlayerScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Story.route) {
            StoryScreen(navController = navController)
        }
        
        composable(Screen.Camera.route) {
            NewCameraScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Screen.Voice.route) {
            VoiceScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(
            route = Screen.StoryPlayer.route,
            arguments = listOf(
                navArgument("storyId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
            StoryPlayerScreen(
                storyId = storyId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}