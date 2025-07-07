package com.mirage.reverie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.mirage.reverie.ui.screens.MainScreen
import dagger.hilt.android.AndroidEntryPoint

// AndroidEntryPoint is used for Hilt (DI)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

//@Preview(showBackground = true, widthDp = 400, heightDp = 850)
@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
fun DefaultPreview() {
    MainScreen()
}