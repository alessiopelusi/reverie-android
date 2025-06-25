package com.mirage.reverie.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun Field(value: String, onNewValue: (String) -> Unit, placeholder: Int, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(placeholder)) },
    )
}

@Composable
fun Field(value: String, errorMessage: String, onNewValue: (String) -> Unit, placeholder: Int, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(placeholder)) },
    )
    ErrorField(errorMessage)
}