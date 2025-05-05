package com.example.reverie

import android.app.admin.PolicyUpdateResult
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reverie.ui.theme.Purple80
import com.example.reverie.ui.theme.ReverieTheme
import com.example.reverie.ui.theme.gialloScuro
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReverieTheme {
                ScaffoldExample()
            }
        }
    }
}

@Serializable object Diary
@Serializable object DiaryHandling

@Composable
fun ScaffoldExample() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { CustomTopBar() },
        bottomBar = { CustomBottomAppBar(navController) },
    ) { innerPadding ->
        NavHost(navController, startDestination = Diary, Modifier.padding(innerPadding)) {
            composable<Diary> {
                DiaryScreen()
            }
            composable<DiaryHandling> {
                DiaryHandlingScreen()
            }
        }
    }
}


@Composable
fun DiaryHandlingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize().border(width = 2.dp, color = Color.Magenta, shape = RectangleShape)
            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val listOfItems: List<String> = (1..10).map { "Item $it" }
        val pagerState = rememberPagerState(pageCount = {
            listOfItems.size
        })

        Text(
            modifier = Modifier.padding(8.dp),
            text = "Emotional Diary"
        )

        HorizontalPager(
            modifier = Modifier
                .border(width = 2.dp, color = Color.Red, shape = RectangleShape)
                .weight(1f, false),
            contentPadding = PaddingValues(50.dp),
            state = pagerState
        ) { page ->
            Card (
                Modifier
                    .padding(8.dp)
                    .graphicsLayer {
                        // Calculate the absolute offset for the current page from the
                        // scroll position. We use the absolute value which allows us to mirror
                        // any effects for both directions
                        val pageOffset = (
                                (pagerState.currentPage - page) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue

                        // We animate the alpha, between 50% and 100%
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        scaleX = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            ) {
                DiaryPage(
                    modifier = Modifier.fillMaxSize(),
                    "Item $page",
                )
            }
        }

        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                )
            }
        }

        val itemsList: List<String> = (1..5).map { "It $it" }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val cornerRadius = 16.dp
            var selectedIndex by remember { mutableStateOf(-1) }

            itemsList.forEachIndexed { index, item ->
                OutlinedButton (
                    onClick = { selectedIndex = index },
                    modifier = when (index) {
                        0 ->
                            Modifier
                                .offset(0.dp, 0.dp)
                                .zIndex(if (selectedIndex == index) 1f else 0f)
                        else ->
                            Modifier
                                .offset((-1 * index).dp, 0.dp)
                                .zIndex(if (selectedIndex == index) 1f else 0f)
                    },
                    shape = when (index) {
                        0 -> RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = 0.dp,
                            bottomStart = cornerRadius,
                            bottomEnd = 0.dp
                        )
                        itemsList.size - 1 -> RoundedCornerShape(
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
                        1.dp, if (selectedIndex == index) {
                            Purple80
                        } else {
                            Purple80.copy(alpha = 0.75f)
                        }
                    ),
                    colors = if (selectedIndex == index) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Purple80.copy(alpha = 0.1f),
                            contentColor = Purple80
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = Purple80
                        )
                    }
                ) {
                    Text(item)
                }
            }
        }
    }

}

@Composable
fun DiaryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize().border(width = 2.dp, color = Color.Magenta, shape = RectangleShape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "Emotional Diary"
        )

        val listOfItems: List<String> = (1..10).map { "Item $it" }
        val diaryPageListState = rememberLazyListState()
        // start lazyrow from the end
        LaunchedEffect(Unit) {
            diaryPageListState.scrollToItem(listOfItems.lastIndex)
        }

        BoxWithConstraints (
            modifier = Modifier.border(width = 2.dp, color = Color.Red, shape = RectangleShape).weight(1f, false),
        ) {
            LazyRow (
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                state = diaryPageListState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = diaryPageListState)
            ) {
                itemsIndexed(listOfItems) { index, item ->
                    Layout(
                        content = {
                            // Here's the content of each list item.
                            val widthFraction = 0.90f
                            DiaryPage(modifier = Modifier
                                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * widthFraction)
                                .aspectRatio(9f/16f),
                                item)
                        },
                        measurePolicy = { measurables, constraints ->
                            // I'm assuming you'll declaring just one root
                            // composable in the content function above
                            // so it's measuring just the Box
                            val placeable = measurables.first().measure(constraints)
                            // maxWidth is from the BoxWithConstraints
                            val maxWidthInPx = maxWidth.roundToPx()
                            // Box width
                            val itemWidth = placeable.width
                            // Calculating the space for the first and last item
                            val startSpace =
                                if (index == 0) (maxWidthInPx - itemWidth) / 2 else 0
                            val endSpace =
                                if (index == listOfItems.lastIndex) (maxWidthInPx - itemWidth) / 2 else 0
                            // The width of the box + extra space
                            val width = startSpace + placeable.width + endSpace
                            layout(width, placeable.height) {
                                // Placing the Box in the right X position
                                val x = if (index == 0) startSpace else 0
                                placeable.place(x, 0)
                            }
                        }
                    )
                }
            }
        }

        /*
        LazyRow(
            modifier = Modifier.border(width = 2.dp, color = Color.Red, shape = RectangleShape).weight(1f, false),
            contentPadding = PaddingValues(
                start = LocalConfiguration.current.screenWidthDp.dp / 10,
                end = LocalConfiguration.current.screenWidthDp.dp / 10
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = diaryPageListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = diaryPageListState)
        ) {
            items(listOfItems, key = String::hashCode) { label:String ->
                val widthFraction = 0.90f
                DiaryPage(modifier = Modifier
                    .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * widthFraction)
                    .aspectRatio(9f/16f),
                    label)
            }
        }
        */

        Text(
            modifier = Modifier.padding(8.dp),
            text = "Page 1/1",
        )
        Button(
            onClick = {},
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
}

@Composable
fun DiaryPage(modifier: Modifier, text: String) {
    Box(
        modifier = modifier
            .border(width = 2.dp, color = Color.Blue, shape = RectangleShape)
            .background(gialloScuro)
    ) {
        Text(text = text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar() {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                Text("Reverie")
                Icon(Icons.Rounded.Person, contentDescription = "Account")
            }
        }
    )
}

@Composable
fun CustomBottomAppBar(navController: NavController) {
    data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

    val topLevelRoutes = listOf(
        TopLevelRoute("Diary", Diary, Icons.Rounded.Person),
        TopLevelRoute("DiaryHandling", DiaryHandling, Icons.Rounded.Phone)
    )

    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        topLevelRoutes.forEach { topLevelRoute ->
            BottomNavigationItem(
                icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                label = { Text(topLevelRoute.name) },
                selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                onClick = {
                    navController.navigate(topLevelRoute.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
/*
    BottomAppBar (
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Row (
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ){
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Menu",
                modifier = Modifier.size(50.dp),
            )
            Icon(
                Icons.Rounded.Home,
                contentDescription = "Menu",
                modifier = Modifier.size(50.dp)
            )
            Icon(
                Icons.Outlined.MailOutline,
                contentDescription = "Account",
                modifier = Modifier.size(50.dp)
            )
        }
    }
*/
}

@Composable
fun MyApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ResizableImage()
        }
    }
}

@Composable
fun ResizableImage() {
    var scale by remember { mutableStateOf(1f) }

    Image(
        painter = painterResource(id = R.drawable.ic_launcher_background), // Sostituisci con un'immagine valida
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    scale *= zoom
                }
            }
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 850)
@Composable
fun DefaultPreview() {
    ReverieTheme {
        ScaffoldExample()
    }
}