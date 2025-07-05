package com.mirage.reverie

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.mirage.reverie.navigation.ProfileRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(navController: NavController) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        title = {
            Text(stringResource(R.string.app_name), textAlign = TextAlign.Center)
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
