package com.mirage.reverie.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SingleLineField(
    value: String,
    onNewValue: (String) -> Unit,
    label: String, modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.CenterEnd
    ) {
        OutlinedTextField(
            singleLine = true,
            modifier = modifier.width(280.dp),
            value = value,
            onValueChange = { onNewValue(it) },
            label = { Text(label) },
            trailingIcon = trailingIcon
        )
    }
}

@Composable
fun SingleLineField(
    value: String,
    errorMessage: String,
    onNewValue: (String) -> Unit,
    label: String, modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        SingleLineField(value, onNewValue, label, modifier, trailingIcon)
        ErrorField(errorMessage)
    }
}