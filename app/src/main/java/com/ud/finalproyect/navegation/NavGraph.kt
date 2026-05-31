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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ud.finalproyect.ui.addmedication.AddMedicationScreen
import com.ud.finalproyect.ui.auth.LoginScreen
import com.ud.finalproyect.ui.calendar.CalendarScreen
import com.ud.finalproyect.ui.diary.DiaryScreen
import com.ud.finalproyect.ui.history.HistoryScreen
import com.ud.finalproyect.ui.home.HomeScreen
import com.ud.finalproyect.ui.settings.SettingsScreen
import com.ud.finalproyect.viewmodel.AuthViewModel

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
fun NavGraph(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val user by authViewModel.user.collectAsState()

    val items = listOf(
        Screen.Home,
        Screen.Diary,
        Screen.Calendar,
        Screen.History,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas donde NO mostramos el tab bar ni el FAB
    val showScaffold = currentRoute != "login" && currentRoute != "add_medication"
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val showFab = currentRoute != "Settings" && showScaffold

    NavHost(
        navController = navController,
        startDestination = if (user != null) Screen.Home.route else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            MainScaffold(
                items = items,
                selectedIndex = selectedIndex,
                showFab = showFab,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onFabClick = { navController.navigate("add_medication") }
            ) {
                HomeScreen(
                    navController = navController,
                    userId = user?.uid ?: ""
                )
            }
        }

        composable(Screen.Diary.route) {
            MainScaffold(
                items = items,
                selectedIndex = selectedIndex,
                showFab = showFab,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onFabClick = { navController.navigate("add_medication") }
            ) {
                DiaryScreen(userId = user?.uid ?: "")
            }
        }

        composable(Screen.Calendar.route) {
            MainScaffold(
                items = items,
                selectedIndex = selectedIndex,
                showFab = showFab,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onFabClick = { navController.navigate("add_medication") }
            ) {
                CalendarScreen()
            }
        }

        composable(Screen.History.route) {
            MainScaffold(
                items = items,
                selectedIndex = selectedIndex,
                showFab = showFab,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onFabClick = { navController.navigate("add_medication") }
            ) {
                HistoryScreen()
            }
        }

        composable(Screen.Settings.route) {
            MainScaffold(
                items = items,
                selectedIndex = selectedIndex,
                showFab = false,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onFabClick = {}
            ) {
                SettingsScreen(
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("add_medication") {
            AddMedicationScreen(
                navController = navController,
                userId = user?.uid ?: ""
            )
        }
    }
}

@Composable
fun MainScaffold(
    items: List<Screen>,
    selectedIndex: Int,
    showFab: Boolean,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp
            ) {
                items.forEachIndexed { index, screen ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { onTabSelected(screen.route) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                FloatingActionButton(onClick = onFabClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add medication")
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}