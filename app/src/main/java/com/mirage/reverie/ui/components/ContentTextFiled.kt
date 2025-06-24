package com.mirage.reverie.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mirage.reverie.R

@Composable
fun ContentTextField(content: String, onUpdateContent: (String) -> Unit) {
    TextField(
        value = content,
        onValueChange = onUpdateContent,
        label = { Text(stringResource(R.string.content)) }
    )
}
