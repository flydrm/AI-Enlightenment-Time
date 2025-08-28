package com.enlightenment.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enlightenment.presentation.ui.screens.home.HomeScreen
import com.enlightenment.presentation.ui.screens.story.StoryScreen
import com.enlightenment.presentation.ui.screens.camera.CameraScreen

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
            CameraScreen(navController = navController)
        }
        
        // TODO: Add other screens as they are implemented
    }
}