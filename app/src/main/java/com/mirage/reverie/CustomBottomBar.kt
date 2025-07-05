package com.mirage.reverie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mirage.reverie.navigation.DiariesRoute
import com.mirage.reverie.navigation.TimeCapsulesRoute

@Composable
fun CustomBottomBar(navController: NavController) {
    data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

    val topLevelRoutes = listOf(
        TopLevelRoute(stringResource(R.string.all_diaries), DiariesRoute, Icons.AutoMirrored.Rounded.LibraryBooks),
        TopLevelRoute(stringResource(R.string.time_capsule), TimeCapsulesRoute, Icons.Rounded.MailOutline)
    )

    Column {
        // "top border"
        HorizontalDivider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(4.dp))

        NavigationBar (
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            topLevelRoutes.forEach { topLevelRoute ->
                NavigationBarItem(
                    icon = { Icon(
                        topLevelRoute.icon,
                        contentDescription = topLevelRoute.name
                    ) },
                    label = { Text(topLevelRoute.name) },
                    //alwaysShowLabel = false, // don't reserve space for label
                    selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                    onClick = {
                        navController.navigate(topLevelRoute.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        //selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.surface
                    ),
                )
            }
        }
    }
}
