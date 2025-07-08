package com.mirage.reverie

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.mirage.reverie.navigation.AllDiariesRoute
import com.mirage.reverie.navigation.AllTimeCapsulesRoute
import com.mirage.reverie.navigation.AuthenticationRoute
import com.mirage.reverie.navigation.CreateDiaryRoute
import com.mirage.reverie.navigation.CreateTimeCapsuleRoute
import com.mirage.reverie.navigation.DiariesRoute
import com.mirage.reverie.navigation.DiaryRoute
import com.mirage.reverie.navigation.EditDiaryPageRoute
import com.mirage.reverie.navigation.EditDiaryRoute
import com.mirage.reverie.navigation.EditProfileRoute
import com.mirage.reverie.navigation.LoginRoute
import com.mirage.reverie.navigation.ProfileRoute
import com.mirage.reverie.navigation.ResetPasswordRoute
import com.mirage.reverie.navigation.SignupRoute
import com.mirage.reverie.navigation.TimeCapsulesRoute
import com.mirage.reverie.navigation.ViewDiaryRoute
import com.mirage.reverie.navigation.ViewProfileRoute
import com.mirage.reverie.navigation.ViewTimeCapsuleRoute
import com.mirage.reverie.ui.screens.AllDiariesScreen
import com.mirage.reverie.ui.screens.AllTimeCapsulesScreen
import com.mirage.reverie.ui.screens.CreateTimeCapsuleScreen
import com.mirage.reverie.ui.screens.EditDiaryPageScreen
import com.mirage.reverie.ui.screens.EditDiaryScreen
import com.mirage.reverie.ui.screens.EditProfileScreen
import com.mirage.reverie.ui.screens.LoginScreen
import com.mirage.reverie.ui.screens.ProfileScreen
import com.mirage.reverie.ui.screens.ResetPasswordScreen
import com.mirage.reverie.ui.screens.SignupScreen
import com.mirage.reverie.ui.screens.ViewDiaryScreen
import com.mirage.reverie.ui.screens.ViewTimeCapsuleScreen

@Composable
fun CustomNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    isUserAuthenticated: Boolean,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
) {

    NavHost(
        navController,
        startDestination = if (isUserAuthenticated) DiariesRoute else AuthenticationRoute,
        Modifier.padding(innerPadding)
    )
    {
        // for each composable we set the visibility for the bottom

        // we create a navigation for Diary that contains ViewDiary and EditDiary
        // in this way we can share the same viewModel between the various composable
        // https://developer.android.com/develop/ui/compose/libraries#hilt-navigation
        navigation<DiariesRoute>(startDestination = AllDiariesRoute) {
            composable<AllDiariesRoute> { backStackEntry ->
                val updatedDiary =
                    backStackEntry.savedStateHandle.get<_root_ide_package_.com.mirage.reverie.data.model.Diary>(
                        "diary"
                    )
                val updatedImages =
                    backStackEntry.savedStateHandle.get<List<_root_ide_package_.com.mirage.reverie.data.model.DiaryImage>>(
                        "diaryImages"
                    )
                backStackEntry.savedStateHandle.remove<_root_ide_package_.com.mirage.reverie.data.model.Diary>(
                    "diary"
                )
                backStackEntry.savedStateHandle.remove<List<_root_ide_package_.com.mirage.reverie.data.model.DiaryImage>>(
                    "diaryImages"
                )

                onBottomBarVisibilityChanged(true)
                AllDiariesScreen(
                    updatedDiary = updatedDiary,
                    updatedImages = updatedImages,
                    onNavigateToEditDiary = { diaryId ->
                        navController.navigate(
                            EditDiaryRoute(
                                diaryId
                            )
                        )
                    },
                    onNavigateToDiary = { diaryId ->
                        navController.navigate(
                            DiaryRoute(
                                diaryId
                            )
                        )
                    },
                    onNavigateToCreateDiary = { navController.navigate(CreateDiaryRoute) }
                )
            }

            composable<EditDiaryRoute> { backStackEntry ->
                onBottomBarVisibilityChanged(true)
                EditDiaryScreen(
                    onComplete = { diary ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("diary", diary)

                        navController.popBackStack()
                    }
                )
            }

            composable<CreateDiaryRoute> { backStackEntry ->
                onBottomBarVisibilityChanged(true)
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
                    val updatedPage =
                        backStackEntry.savedStateHandle.get<_root_ide_package_.com.mirage.reverie.data.model.DiaryPage>(
                            "page"
                        )
                    backStackEntry.savedStateHandle.remove<_root_ide_package_.com.mirage.reverie.data.model.DiaryPage>(
                        "page"
                    )

                    onBottomBarVisibilityChanged(true)
                    ViewDiaryScreen(
                        updatedPage = updatedPage,
                        onNavigateToEditDiaryPage = { page ->
                            navController.navigate(
                                EditDiaryPageRoute(
                                    page
                                )
                            )
                        },
                        onComplete = { diaryImages ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("diaryImages", diaryImages)

                            navController.popBackStack()
                        }
                    )
                }

                composable<EditDiaryPageRoute> { backStackEntry ->
                    onBottomBarVisibilityChanged(false)
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

        navigation<AuthenticationRoute>(
            startDestination = LoginRoute
        ) {
            composable<LoginRoute> {
                onBottomBarVisibilityChanged(false)
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(AllDiariesRoute) {
                            popUpTo(LoginRoute) {
                                inclusive = true
                            }
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
                onBottomBarVisibilityChanged(false)
                SignupScreen(
                    onSignupSuccess = {
                        navController.navigate(DiariesRoute) {
                            popUpTo(LoginRoute) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable<ResetPasswordRoute> {
                onBottomBarVisibilityChanged(false)
                ResetPasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        navigation<TimeCapsulesRoute>(
            startDestination = AllTimeCapsulesRoute
        ) {
            composable<AllTimeCapsulesRoute> { backStackEntry ->
                onBottomBarVisibilityChanged(true)

                val newTimeCapsule =
                    backStackEntry.savedStateHandle.get<_root_ide_package_.com.mirage.reverie.data.model.TimeCapsule>(
                        "timeCapsule"
                    )
                backStackEntry.savedStateHandle.remove<_root_ide_package_.com.mirage.reverie.data.model.TimeCapsule>(
                    "timeCapsule"
                )

                AllTimeCapsulesScreen(
                    newTimeCapsule = newTimeCapsule,
                    onNavigateToCreateTimeCapsule = { navController.navigate(CreateTimeCapsuleRoute) },
                    onNavigateToViewTimeCapsule = { timeCapsuleId, timeCapsuleType ->
                        navController.navigate(
                            ViewTimeCapsuleRoute(
                                timeCapsuleId,
                                timeCapsuleType
                            )
                        )
                    }
                )
            }

            composable<ViewTimeCapsuleRoute> {
                onBottomBarVisibilityChanged(true)
                ViewTimeCapsuleScreen(onViewProfile = { uid ->
                    navController.navigate(
                        ViewProfileRoute(
                            uid
                        )
                    )
                })
            }

            composable<CreateTimeCapsuleRoute> { backStackEntry ->
                onBottomBarVisibilityChanged(true)
                CreateTimeCapsuleScreen(
                    onComplete = { timeCapsule ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("timeCapsule", timeCapsule)

                        navController.popBackStack()
                    }
                )
            }
        }

        navigation<ProfileRoute>(startDestination = ViewProfileRoute::class) {
            composable<ViewProfileRoute> { backStackEntry ->
                onBottomBarVisibilityChanged(false)
                val updatedProfile =
                    backStackEntry.savedStateHandle.get<_root_ide_package_.com.mirage.reverie.data.model.User>(
                        "profile"
                    )
                backStackEntry.savedStateHandle.remove<_root_ide_package_.com.mirage.reverie.data.model.User>(
                    "profile"
                )

                ProfileScreen(
                    onEditProfile = { profileId ->
                        navController.navigate(
                            EditProfileRoute(
                                profileId
                            )
                        )
                    },
                    onLogout = onLogout,
                    updatedProfile = updatedProfile
                )
            }

            composable<EditProfileRoute> {
                onBottomBarVisibilityChanged(false)
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