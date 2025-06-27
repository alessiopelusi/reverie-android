package com.mirage.reverie.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mirage.reverie.R

@Composable
fun PasswordField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier, label: String = stringResource(R.string.password)) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier.width(280.dp),
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation()
        //leadingIcon = { [...] }
    )
}

@Composable
fun PasswordField(value: String, errorMessage: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier, label: String = stringResource(R.string.password)) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        PasswordField(value, onNewValue, modifier, label)
        ErrorField(errorMessage)
    }
}
