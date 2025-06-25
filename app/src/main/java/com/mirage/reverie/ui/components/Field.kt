package com.mirage.reverie.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Field(value: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) }
    )
}

@Composable
fun Field(value: String, errorMessage: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) }
    )
    ErrorField(errorMessage)
}