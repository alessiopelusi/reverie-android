package com.example.reverie

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Serializable object ModifyDiary

@Composable
fun ModifyDiaryScreen(){
    Text(
        modifier = Modifier.padding(8.dp),
        text = "You are modifying your diary!",
    )
}