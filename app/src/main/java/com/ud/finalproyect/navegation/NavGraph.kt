package com.ud.finalproyect.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ud.finalproyect.ui.addmedication.AddMedicationScreen
import com.ud.finalproyect.ui.calendar.CalendarScreen
import com.ud.finalproyect.ui.diary.DiaryScreen
import com.ud.finalproyect.ui.history.HistoryScreen
import com.ud.finalproyect.ui.home.HomeScreen
import com.ud.finalproyect.ui.settings.SettingsScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("Home", "Home", Icons.Default.Home)
    object Diary : Screen("Diary", "Diary", Icons.Default.Today)
    object Calendar : Screen("Calendar", "Calendar", Icons.Default.CalendarMonth)
    object History : Screen("Historial", "Historial", Icons.Default.History)
    object Settings : Screen("Settings", "Settings", Icons.Default.Settings)
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Diary,
        Screen.Calendar,
        Screen.History,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val showFab = currentRoute != "Settings"

    Scaffold(
        topBar = {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                items.forEachIndexed { index, screen ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(screen.title)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("add_medication")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add medication")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Diary.route) {
                DiaryScreen()
            }
            composable(Screen.Calendar.route) {
                CalendarScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable("add_medication") {
                AddMedicationScreen(navController = navController)
            }
        }
    }
}