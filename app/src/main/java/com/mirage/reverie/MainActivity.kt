package com.mirage.reverie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.mirage.reverie.ui.theme.ReverieTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint

// AndroidEntryPoint is used for Hilt (DI)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainComposable()
        }
    }
}

@Composable
fun MainComposable(
) {
    ReverieTheme (
        darkTheme = false
    ){
        val navController = rememberNavController()
        var bottomBarVisibility by remember { mutableStateOf(true) }

        Scaffold(
            topBar = { CustomTopBar(navController) },
            // if bottomBarVisibility is set to none, we don't show the bottom bar
            bottomBar = { if (bottomBarVisibility) CustomBottomBar(navController) },
        ) { innerPadding ->
            CustomNavHost(
                navController = navController,
                innerPadding = innerPadding,
                onBottomBarVisibilityChanged = { bottomBarVisibility = it }
            )
        }
    }
}

//@Preview(showBackground = true, widthDp = 400, heightDp = 850)
@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
fun DefaultPreview() {
    MainComposable()
}