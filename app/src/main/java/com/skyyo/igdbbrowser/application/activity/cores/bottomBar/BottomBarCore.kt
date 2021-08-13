package com.skyyo.igdbbrowser.application.activity.cores.bottomBar

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.skyyo.igdbbrowser.application.Screens
import com.skyyo.igdbbrowser.application.activity.PopulatedNavHost
import com.skyyo.igdbbrowser.extensions.log
import com.skyyo.igdbbrowser.extensions.navigateToRootDestination

@Composable
@ExperimentalAnimationApi
fun BottomBarCore(
    bottomBarScreens: List<Screens>,
    startDestination: String,
    navController: NavHostController
) {
    val isBottomBarVisible = rememberSaveable { mutableStateOf(false) }
    val selectedTab = rememberSaveable { mutableStateOf(0) }

    DisposableEffect(navController) {
        val callback = NavController.OnDestinationChangedListener { _, destination, args ->
            log("${destination.route}")
            log("first destination:${navController.findDestination(navController.graph.findStartDestination().id)}")
            when (destination.route) {
                Screens.AuthScreen.route -> isBottomBarVisible.value = false
                else -> isBottomBarVisible.value = true
            }
        }
        navController.addOnDestinationChangedListener(callback)
        onDispose {
            navController.removeOnDestinationChangedListener(callback)
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedBottomBar(
                bottomBarScreens,
                selectedTab.value,
                isBottomBarVisible.value
            ) { index, route ->
                selectedTab.value = index
                navController.navigateToRootDestination(route)
            }
        },
        content = { innerPadding ->
            PopulatedNavHost(
                startDestination = startDestination,
                innerPadding = innerPadding,
                navController = navController,
                onBackPressIntercepted = {
                    selectedTab.value = 0
                    navController.navigateToRootDestination(Screens.DogFeedScreen.route)
                }
            )
        })
}