package com.example.smartscheduler.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.smartscheduler.ui.screens.DashboardScreen
import com.example.smartscheduler.ui.screens.MainScreen
import com.example.smartscheduler.ui.screens.SchedulerScreen
import com.example.smartscheduler.Screen

@Composable
fun AppNavigation(viewModel: MachineViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Dashboard.route) },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Główna") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.MachineList.route) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Maszyny") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Scheduler.route) },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Grafik") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
            composable(Screen.MachineList.route) { MainScreen(viewModel) }
            composable(Screen.Scheduler.route) { SchedulerScreen(viewModel) }
        }
    }
}