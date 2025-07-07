package com.mirage.reverie.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.mirage.reverie.CustomBottomBar
import com.mirage.reverie.CustomNavHost
import com.mirage.reverie.CustomTopBar
import com.mirage.reverie.navigation.LoginRoute
import com.mirage.reverie.navigation.ProfileRoute
import com.mirage.reverie.ui.theme.ReverieTheme
import com.mirage.reverie.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    ReverieTheme (
        darkTheme = false
    ){
        val navController = rememberNavController()
        var bottomBarVisibility by remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                CustomTopBar {
                    navController.navigate(ProfileRoute(viewModel.getUserId())) {
                        popUpTo(ProfileRoute::class) { saveState = true }
                        launchSingleTop = true
                    }
                }
            },
            // if bottomBarVisibility is set to none, we don't show the bottom bar
            bottomBar = { if (bottomBarVisibility) CustomBottomBar(navController) },
        ) { innerPadding ->
            CustomNavHost(
                navController = navController,
                innerPadding = innerPadding,
                isUserAuthenticated = viewModel.isUserAuthenticated(),
                onBottomBarVisibilityChanged = { bottomBarVisibility = it },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
