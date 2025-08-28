package com.enlightenment.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Story : Screen("story")
    object StoryDetail : Screen("story/{storyId}") {
        fun createRoute(storyId: String) = "story/$storyId"
    }
    object StoryPlayer : Screen("story_player/{storyId}") {
        fun createRoute(storyId: String) = "story_player/$storyId"
    }
    object Camera : Screen("camera")
    object Voice : Screen("voice")
    object Achievement : Screen("achievement")
    object Settings : Screen("settings")
    object ParentControl : Screen("parent_control")
}