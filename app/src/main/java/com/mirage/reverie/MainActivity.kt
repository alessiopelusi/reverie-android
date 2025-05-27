package com.mirage.reverie

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.mirage.reverie.ui.theme.ReverieTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.navigation
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.User
import com.mirage.reverie.navigation.AllDiariesRoute
import com.mirage.reverie.navigation.AuthenticationRoute
import com.mirage.reverie.navigation.DiariesRoute
import com.mirage.reverie.navigation.DiaryRoute
import com.mirage.reverie.navigation.ViewDiaryRoute
import com.mirage.reverie.navigation.EditDiaryPageRoute
import com.mirage.reverie.navigation.EditDiaryRoute
import com.mirage.reverie.navigation.EditProfileRoute
import com.mirage.reverie.navigation.LoginRoute
import com.mirage.reverie.navigation.ResetPasswordRoute
import com.mirage.reverie.navigation.SignupRoute
import com.mirage.reverie.navigation.ProfileRoute
import com.mirage.reverie.navigation.ViewProfileRoute
import com.mirage.reverie.ui.screens.AllDiariesScreen
import com.mirage.reverie.ui.screens.EditDiaryPageScreen
import com.mirage.reverie.ui.screens.EditDiaryScreen
import com.mirage.reverie.ui.screens.EditProfileScreen
import com.mirage.reverie.ui.screens.LoginScreen
import com.mirage.reverie.ui.screens.ViewDiaryScreen
import com.mirage.reverie.ui.screens.ProfileScreen
import com.mirage.reverie.ui.screens.ResetPasswordScreen
import com.mirage.reverie.ui.screens.SignupScreen
import com.mirage.reverie.ui.theme.PaperColor
import com.mirage.reverie.viewmodel.AllDiariesUiState
import com.mirage.reverie.viewmodel.ModalNavigationDrawerUiState
import com.mirage.reverie.viewmodel.ModalNavigationDrawerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// AndroidEntryPoint is used for Hilt (DI)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainComposable()
        }
    }
}

fun isUserAuthenticated(): Boolean {
    return FirebaseAuth.getInstance().currentUser != null
}

fun logout() {
    FirebaseAuth.getInstance().signOut()
}

fun getUserId() : String {
    return FirebaseAuth.getInstance().uid.toString()
}

@Composable
fun MainComposable(
    viewModel: ModalNavigationDrawerViewModel = hiltViewModel()
) {
    ReverieTheme {
        val scope = rememberCoroutineScope()

        val navController = rememberNavController()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ModalNavigationDrawer(
            drawerContent = {
                when (uiState) {
                    is ModalNavigationDrawerUiState.Loading -> CircularProgressIndicator()
                    is ModalNavigationDrawerUiState.Success -> {
                        val diaries = (uiState as ModalNavigationDrawerUiState.Success).diaries
                        ModalDrawerSheet {
                            LazyColumn(
                                modifier = Modifier.padding(horizontal = 16.dp),
//                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
//                                Spacer(Modifier.height(12.dp))
                                item {
                                    Text(
                                        "Impostazioni",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                item{
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                item{
                                    Text(
                                        "I tuoi Diari",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                itemsIndexed (diaries) { index, diary ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 32.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            diaries[index].title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton (
                                                // replace pagerState.currentPage with the actual id of the currentPage diary
//                                                onClick = { onNavigateToEditDiary(currentDiary.id) },
                                                onClick = {
                                                    scope.launch {
                                                        drawerState.close()
                                                        navController.navigate(EditDiaryRoute(diaries[index].id))
                                                    }
                                                },
                                                colors = IconButtonColors(
                                                    containerColor = PaperColor,
                                                    contentColor = MaterialTheme.colorScheme.primary,
                                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    disabledContentColor = MaterialTheme.colorScheme.primary
                                                ),
//                                                modifier = Modifier
//                                                    .align(Alignment.Bottom),
                                            ) {
                                                Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.onDeleteDiary(
                                                        diaries[index].id,
                                                    )
                                                    scope.launch {
                                                        drawerState.close()
                                                    }
                                                },
                                                colors = IconButtonColors(
                                                    containerColor = Color.White,
                                                    contentColor = MaterialTheme.colorScheme.primary,
                                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    disabledContentColor = MaterialTheme.colorScheme.primary
                                                ),
//                                                modifier = Modifier
//                                                    .align(Alignment.Bottom),
                                            ) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                                            }
                                        }

                                    }
                                }
                                item{
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.onCreateDiary(
                                                    onSuccess = { diary ->
                                                        scope.launch {
                                                            drawerState.close()
                                                            navController.navigate(EditDiaryRoute(diary.id))
                                                        }
                                                    },
                                                )
                                            },
                                            modifier = Modifier.padding(8.dp),
                                        ) {
                                            Text(
                                                "Crea nuovo Diario",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                }
                                item{
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                item{
                                    Text(
                                        "Altro",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    //                        NavigationDrawerItem(
                                    //                            label = { Text("Settings") },
                                    //                            selected = false,
                                    //                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                    //                            badge = { Text("20") }, // Placeholder
                                    //                            onClick = { /* Handle click */ }
                                    //                        )
                                    NavigationDrawerItem(
                                        label = { Text("Logout") },
                                        selected = false,
                                        icon = {
                                            Icon(
                                                Icons.AutoMirrored.Outlined.ExitToApp,
                                                contentDescription = null
                                            )
                                        },
                                        badge = { Text("20") }, // Placeholder
                                        onClick = {
                                            logout()
                                            navController.navigate(LoginRoute) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            scope.launch {
                                                drawerState.close()
                                            }
                                        }
                                    )
                                    NavigationDrawerItem(
                                        label = { Text("Aiuto e feedback") },
                                        selected = false,
                                        icon = {
                                            Icon(
                                                Icons.AutoMirrored.Outlined.Help,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = { /* Handle click */ },
                                    )
                                }
                            }
                        }
                    }
                    is ModalNavigationDrawerUiState.Error -> Text(text = "Error: ${(uiState as ModalNavigationDrawerUiState.Error).exception.message}")
                }
            },
            drawerState = drawerState
        ) {
            var bottomBarVisibility by remember { mutableStateOf(true) }

            Scaffold(
                topBar = { CustomTopBar(navController, drawerState) },
                // if bottomBarVisibility is set to none, we don't show the bottom bar
                bottomBar = { if (bottomBarVisibility) CustomBottomBar(navController) },
            ) { innerPadding ->
                NavHost(
                    navController,
                    startDestination = if (isUserAuthenticated()) DiariesRoute else AuthenticationRoute,
                    Modifier.padding(innerPadding)
                ) {
                    // for each composable we set the visibility for the bottom

                    // we create a navigation for Diary that contains ViewDiary and EditDiary
                    // in this way we can share the same viewModel between the various composable
                    // https://developer.android.com/develop/ui/compose/libraries#hilt-navigation
                    navigation<DiariesRoute>(startDestination = AllDiariesRoute) {
                        composable<AllDiariesRoute> { backStackEntry ->
                            val updatedDiary = backStackEntry.savedStateHandle.get<Diary>("diary")
                            backStackEntry.savedStateHandle.remove<User>("profile")

                            bottomBarVisibility = true
                            AllDiariesScreen(
                                updatedDiary = updatedDiary,
                                onNavigateToEditDiary = {diaryId -> navController.navigate(EditDiaryRoute(diaryId))},
                                onNavigateToDiary = {diaryId -> navController.navigate(DiaryRoute(diaryId))}
                            )
                        }

                        composable<EditDiaryRoute> { backStackEntry ->
                            bottomBarVisibility = true
                            EditDiaryScreen(
                                onComplete = { diary ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("diary", diary)

                                    navController.popBackStack()
                                }
                            )
                        }

                        navigation<DiaryRoute>(startDestination = ViewDiaryRoute::class) {
                            composable<ViewDiaryRoute> { backStackEntry ->
                                val updatedPage = backStackEntry.savedStateHandle.get<DiaryPage>("page")
                                backStackEntry.savedStateHandle.remove<DiaryPage>("page")

                                bottomBarVisibility = true
                                ViewDiaryScreen(
                                    updatedPage = updatedPage,
                                    onNavigateToEditDiaryPage = { page ->
                                        navController.navigate(EditDiaryPageRoute(page))
                                    }
                                )
                            }
                            composable<EditDiaryPageRoute> { backStackEntry ->
                                bottomBarVisibility = false
                                EditDiaryPageScreen(
                                    onComplete = { page ->
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("page", page)

                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }

                    navigation<AuthenticationRoute>(startDestination = LoginRoute) {
                        composable<LoginRoute> {
                            bottomBarVisibility = false
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(AllDiariesRoute) {
                                        popUpTo(LoginRoute) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(SignupRoute)
                                },
                                onNavigateToResetPassword = {
                                    navController.navigate(ResetPasswordRoute)
                                }
                            )
                        }

                        composable<SignupRoute> {
                            bottomBarVisibility = false
                            SignupScreen (
                                onSignupSuccess = {
                                    navController.popBackStack()  // Torna al login dopo registrazione
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<ResetPasswordRoute> {
                            bottomBarVisibility = false
                            ResetPasswordScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable<TimeCapsuleRoute> {
                        bottomBarVisibility = true
                        TimeCapsuleScreen()
                    }

                    navigation<ProfileRoute>(startDestination = ViewProfileRoute::class) {
                        composable<ViewProfileRoute>{ backStackEntry ->
                            bottomBarVisibility = false
                            val updatedProfile = backStackEntry.savedStateHandle.get<User>("profile")
                            backStackEntry.savedStateHandle.remove<User>("profile")

                            ProfileScreen(
                                onEditProfile = { profileId ->
                                    navController.navigate(EditProfileRoute(profileId))
                                },
                                updatedProfile = updatedProfile
                            )
                        }

                        composable<EditProfileRoute>{
                            bottomBarVisibility = false
                            EditProfileScreen(
                                onComplete = { profile ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("profile", profile)

                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(navController: NavController, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
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
            IconButton(onClick = {
                navController.navigate(ProfileRoute(getUserId())) {
                    popUpTo(ProfileRoute::class) { saveState = true }
                    launchSingleTop = true
                }
            }) {
                Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.account))
            }
        }
    )
}

@Composable
fun CustomBottomBar(navController: NavController) {
    data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

    val topLevelRoutes = listOf(
        TopLevelRoute(stringResource(R.string.all_diaries), DiariesRoute, Icons.AutoMirrored.Rounded.LibraryBooks),
        TopLevelRoute(stringResource(R.string.time_capsule), TimeCapsuleRoute, Icons.Rounded.MailOutline)
    )

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

//@Preview(showBackground = true, widthDp = 400, heightDp = 850)
@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
fun DefaultPreview() {
    MainComposable()
}