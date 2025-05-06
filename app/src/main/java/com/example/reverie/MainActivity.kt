package com.example.reverie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reverie.ui.theme.ReverieTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Reverie()
        }
    }
}

@Composable
fun Reverie() {
    ReverieTheme {
        val navController = rememberNavController()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Drawer Title",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        HorizontalDivider()

                        Text(
                            "Section 1",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawerItem(
                            label = { Text("Item 1") },
                            selected = false,
                            onClick = { /* Handle click */ }
                        )
                        NavigationDrawerItem(
                            label = { Text("Item 2") },
                            selected = false,
                            onClick = { /* Handle click */ }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            "Section 2",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawerItem(
                            label = { Text("Settings") },
                            selected = false,
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            badge = { Text("20") }, // Placeholder
                            onClick = { /* Handle click */ }
                        )
                        NavigationDrawerItem(
                            label = { Text("Help and feedback") },
                            selected = false,
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Help,
                                    contentDescription = null
                                )
                            },
                            onClick = { /* Handle click */ },
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            },
            drawerState = drawerState
        ) {
            var bottomBarVisibility by remember { mutableStateOf(true) }

            Scaffold(
                topBar = { CustomTopBar(drawerState) },
                // if bottomBarVisibility is set to none, we don't show the bottom bar
                bottomBar = { if (bottomBarVisibility) CustomBottomBar(navController) },
            ) { innerPadding ->
                NavHost(navController, startDestination = Diary, Modifier.padding(innerPadding)) {
                    // for each composable we set the visibility for the bottom
                    composable<AllDiaries> {
                        bottomBarVisibility = true
                        AllDiariesScreen()
                    }
                    composable<Diary> {
                        bottomBarVisibility = true
                        DiaryScreen()
                    }
                    composable<TimeCapsule> {
                        bottomBarVisibility = true
                        TimeCapsuleScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(stringResource(R.string.app_name), textAlign = TextAlign.Center)
        },
        navigationIcon = {
            IconButton (onClick = {
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()
                    } else {
                        drawerState.close()
                    }
                }
            }) {
                Icon(Icons.Rounded.Menu, contentDescription = stringResource(R.string.navigation_drawer))
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.account))
            }
        }
    )
}

@Composable
fun CustomBottomBar(navController: NavController) {
    data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

    val topLevelRoutes = listOf(
        TopLevelRoute(stringResource(R.string.all_diaries), AllDiaries, Icons.AutoMirrored.Rounded.LibraryBooks),
        TopLevelRoute(stringResource(R.string.diary), Diary, Icons.AutoMirrored.Rounded.MenuBook),
        TopLevelRoute(stringResource(R.string.time_capsule), TimeCapsule, Icons.Rounded.MailOutline)
    )

    NavigationBar (
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        topLevelRoutes.forEach { topLevelRoute ->
            NavigationBarItem(
                icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                //label = { Text(topLevelRoute.name) },
                alwaysShowLabel = false, // don't reserve space for label
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
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 850)
@Composable
fun DefaultPreview() {
    Reverie()
}