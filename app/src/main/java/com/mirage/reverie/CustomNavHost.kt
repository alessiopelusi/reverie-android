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

@Composable
fun CustomNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
) {

    NavHost(
        navController,
        startDestination = if (isUserAuthenticated()) DiariesRoute else AuthenticationRoute,
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
                _root_ide_package_.com.mirage.reverie.ui.screens.AllDiariesScreen(
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
                _root_ide_package_.com.mirage.reverie.ui.screens.EditDiaryScreen(
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
                _root_ide_package_.com.mirage.reverie.ui.screens.EditDiaryScreen(
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
                    _root_ide_package_.com.mirage.reverie.ui.screens.ViewDiaryScreen(
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
                    _root_ide_package_.com.mirage.reverie.ui.screens.EditDiaryPageScreen(
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
                _root_ide_package_.com.mirage.reverie.ui.screens.LoginScreen(
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
                _root_ide_package_.com.mirage.reverie.ui.screens.SignupScreen(
                    onSignupSuccess = {
                        navController.popBackStack()  // Torna al login dopo registrazione
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable<ResetPasswordRoute> {
                onBottomBarVisibilityChanged(false)
                _root_ide_package_.com.mirage.reverie.ui.screens.ResetPasswordScreen(
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

                _root_ide_package_.com.mirage.reverie.ui.screens.AllTimeCapsulesScreen(
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
                _root_ide_package_.com.mirage.reverie.ui.screens.ViewTimeCapsuleScreen(onViewProfile = { uid ->
                    navController.navigate(
                        ViewProfileRoute(
                            uid
                        )
                    )
                })
            }

            composable<CreateTimeCapsuleRoute> { backStackEntry ->
                onBottomBarVisibilityChanged(true)
                _root_ide_package_.com.mirage.reverie.ui.screens.CreateTimeCapsuleScreen(
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

                _root_ide_package_.com.mirage.reverie.ui.screens.ProfileScreen(
                    onEditProfile = { profileId ->
                        navController.navigate(
                            EditProfileRoute(
                                profileId
                            )
                        )
                    },
                    onLogout = {
                        logout()
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    updatedProfile = updatedProfile
                )
            }

            composable<EditProfileRoute> {
                onBottomBarVisibilityChanged(false)
                _root_ide_package_.com.mirage.reverie.ui.screens.EditProfileScreen(
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