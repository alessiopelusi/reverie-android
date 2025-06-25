package com.mirage.reverie.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.mirage.reverie.R

@Composable
fun PasswordField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier, label: String = stringResource(R.string.password)) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation()
        //leadingIcon = { [...] }
    )
}

@Composable
fun PasswordField(value: String, errorMessage: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier, label: String = stringResource(R.string.password)) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation()
        //leadingIcon = { [...] }
    )
    ErrorField(errorMessage)
}
