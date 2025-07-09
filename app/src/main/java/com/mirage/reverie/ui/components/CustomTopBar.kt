package com.mirage.reverie.ui.components

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
import com.mirage.reverie.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    onNavigateToProfile: () -> Unit
) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        title = {
            Text(stringResource(R.string.app_name), textAlign = TextAlign.Center)
        },
        actions = {
            IconButton(onClick = onNavigateToProfile) {
                Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.account))
            }
        }
    )
}
