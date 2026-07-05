package com.qaspro.worldexplorer.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qaspro.worldexplorer.screens.country.CountryScreen
import com.qaspro.worldexplorer.screens.lesson.LessonScreen
import com.qaspro.worldexplorer.screens.worldmap.WorldMapScreen

/** The whole navigation graph: World browser -> Country page -> Lesson. */
@Composable
fun WorldExplorerApp() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.MAP,
        enterTransition = { scaleIn(initialScale = 0.92f, animationSpec = tween(320)) + fadeIn(tween(320)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(240)) },
        popExitTransition = { slideOutVertically(tween(280)) { it / 6 } + fadeOut(tween(240)) }
    ) {
        composable(Routes.MAP) {
            WorldMapScreen(
                onCountryTap = { id -> nav.navigate(Routes.country(id)) }
            )
        }

        composable(
            route = Routes.COUNTRY,
            arguments = listOf(navArgument("countryId") { type = NavType.StringType }),
            enterTransition = { slideInVertically(tween(340)) { it / 5 } + fadeIn(tween(340)) }
        ) { backStack ->
            val id = backStack.arguments?.getString("countryId").orEmpty()
            CountryScreen(
                countryId = id,
                onBack = { nav.popBackStack() },
                onOpenLesson = { lessonIndex -> nav.navigate(Routes.lesson(id, lessonIndex)) }
            )
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType },
                navArgument("lessonIndex") { type = NavType.IntType }
            )
        ) { backStack ->
            val id = backStack.arguments?.getString("countryId").orEmpty()
            val lessonIndex = backStack.arguments?.getInt("lessonIndex") ?: 0
            LessonScreen(
                countryId = id,
                lessonIndex = lessonIndex,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
