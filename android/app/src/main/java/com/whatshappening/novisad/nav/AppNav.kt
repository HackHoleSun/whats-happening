package com.whatshappening.novisad.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whatshappening.novisad.ui.screens.detail.EventDetailRoute
import com.whatshappening.novisad.ui.screens.home.HomeRoute
import com.whatshappening.novisad.ui.screens.map.MapRoute
import com.whatshappening.novisad.ui.screens.saved.SavedRoute
import com.whatshappening.novisad.ui.screens.search.SearchRoute

// ── AppNav ────────────────────────────────────────────────────────────────────

/**
 * Root NavHost wiring all five routes together.
 *
 * Transitions:
 *  - Default: subtle fade + slight upward slide on push; reverse on pop
 *  - Detail screen: more pronounced slide-up to feel like a "lift"
 *
 * Bottom-nav tabs use `popUpTo(Routes.Home) { saveState = true }` so back-stack
 * doesn't grow unboundedly when switching between Home / Map / Saved.
 */
@Composable
fun AppNav(startDestination: String = Routes.Home) {
    val nav = rememberNavController()

    NavHost(
        navController    = nav,
        startDestination = startDestination,
        enterTransition  = {
            fadeIn(tween(180)) + slideIntoContainer(
                towards         = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec   = tween(220, easing = FastOutSlowInEasing),
                initialOffset   = { it / 12 },
            )
        },
        exitTransition = { fadeOut(tween(120)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition  = {
            fadeOut(tween(120)) + slideOutOfContainer(
                towards       = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(220),
                targetOffset  = { it / 12 },
            )
        },
    ) {
        // ── Home ──────────────────────────────────────────────────────────────
        composable(Routes.Home) {
            HomeRoute(
                onEventClick  = { nav.navigate(Routes.detail(it.id)) },
                onSearchClick = { nav.navigate(Routes.Search) },
                onMapClick    = {
                    nav.navigate(Routes.Map) {
                        popUpTo(Routes.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                onSavedClick  = {
                    nav.navigate(Routes.Saved) {
                        popUpTo(Routes.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
            )
        }

        // ── Detail ────────────────────────────────────────────────────────────
        composable(
            route     = Routes.Detail,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                ) + fadeIn(tween(180))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards       = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(280),
                ) + fadeOut(tween(120))
            },
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId").orEmpty()
            EventDetailRoute(
                eventId = eventId,
                onBack  = { nav.popBackStack() },
            )
        }

        // ── Search ────────────────────────────────────────────────────────────
        composable(Routes.Search) {
            SearchRoute(
                onBack       = { nav.popBackStack() },
                onEventClick = { nav.navigate(Routes.detail(it.id)) },
            )
        }

        // ── Map ───────────────────────────────────────────────────────────────
        composable(Routes.Map) {
            MapRoute(
                onEventClick = { nav.navigate(Routes.detail(it.id)) },
                onListClick  = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
                onHomeClick  = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
                onSavedClick = {
                    nav.navigate(Routes.Saved) {
                        popUpTo(Routes.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
            )
        }

        // ── Saved ─────────────────────────────────────────────────────────────
        composable(Routes.Saved) {
            SavedRoute(
                onEventClick = { nav.navigate(Routes.detail(it.id)) },
                onHomeClick  = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
                onMapClick   = {
                    nav.navigate(Routes.Map) {
                        popUpTo(Routes.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
            )
        }
    }
}
