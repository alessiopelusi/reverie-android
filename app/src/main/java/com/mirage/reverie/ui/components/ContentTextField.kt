package com.mirage.reverie.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContentTextField(value: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        modifier = modifier.width(280.dp),
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) }
    )
}

@Composable
fun ContentTextField(value: String, errorMessage: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        ContentTextField(value, onNewValue, label, modifier)
        ErrorField(errorMessage)
    }
}
