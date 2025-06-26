package com.mirage.reverie.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mirage.reverie.ui.theme.Purple80
import com.mirage.reverie.viewmodel.ButtonState
import com.mirage.reverie.viewmodel.TimeCapsuleType

@Composable
fun <T> ButtonBar(buttonState: T, buttonElements: List<T>, onButtonStateUpdate: (T) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val cornerRadius = 16.dp

        buttonElements.forEachIndexed { index, item ->
            OutlinedButton(
                onClick = {
                    onButtonStateUpdate(item)
                },
                shape = when (index) {
                    0 -> RoundedCornerShape(
                        topStart = cornerRadius,
                        topEnd = 0.dp,
                        bottomStart = cornerRadius,
                        bottomEnd = 0.dp
                    )

                    buttonElements.size - 1 -> RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = cornerRadius,
                        bottomStart = 0.dp,
                        bottomEnd = cornerRadius
                    )

                    else -> RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                },
                border = BorderStroke(
                    1.dp, if (buttonState == item) {
                        Purple80
                    } else {
                        Purple80.copy(alpha = 0.75f)
                    }
                ),
                colors = if (buttonState == item) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Purple80.copy(alpha = 0.1f),
                        contentColor = Purple80
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = Purple80
                    )
                }
            ) {
                Text(item.toString())
            }
        }
    }
}