package com.mirage.reverie.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SingleLineField(value: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) }
    )
}

@Composable
fun SingleLineField(value: String, errorMessage: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        SingleLineField(value, onNewValue, label, modifier)
        ErrorField(errorMessage)
    }
}