package com.ud.finalproyect.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val items = listOf("Home", "Diary", "Calendar", "Historial", "Settings")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedIndex = items.indexOfFirst { it == currentRoute }.coerceAtLeast(0)

    // Determinar si mostrar el FAB (todas excepto Settings)
    val showFab = currentRoute != "Settings"

    Scaffold(
        topBar = {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                items.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {
                            navController.navigate(title) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        text = { Text(title) }
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
            startDestination = "Home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Home") {
                HomeScreen()
            }
            composable("Diary") {
                DiaryScreen()
            }
            composable("Calendar") {
                CalendarScreen()
            }
            composable("Historial") {
                HistoryScreen()
            }
            composable("Settings") {
                SettingsScreen()
            }
            composable("add_medication") {
                AddMedicationScreen(navController = navController)
            }
        }
    }
}