package com.example.smartscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartscheduler.data.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicjalizacja bazy i repozytorium
        val database = AppDatabase.getDatabase(this)
        val repository = MachineRepository(database.machineDao())

        setContent {
            MaterialTheme {
                AppNavigation(repository)
            }
        }
    }
}

@Composable
fun AppNavigation(repository: MachineRepository) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("dashboard") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Główna") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("machines") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Maszyny") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("scheduler") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Grafik") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "machines",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen() }
            composable("machines") { MainScreen(repository) }
            composable("scheduler") { SchedulerScreen(repository) }
        }
    }
}

@Composable
fun MainScreen(repository: MachineRepository) {
    val scope = rememberCoroutineScope()
    val machines by repository.allMachines.collectAsState(initial = emptyList())

    var name by remember { mutableStateOf("") }
    var power by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Zarządzanie Maszynami", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = power, onValueChange = { power = it }, label = { Text("Moc (kW)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Czas (h)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    if (name.isNotBlank()) {
                        repository.addMachine(MachineEntity(
                            name = name,
                            powerConsumptionKw = power.toDoubleOrNull() ?: 0.0,
                            durationHours = duration.toIntOrNull() ?: 1,
                            priority = 1
                        ))
                        name = ""; power = ""; duration = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text("Dodaj Maszynę")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(machines) { machine ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = machine.name, fontWeight = FontWeight.Bold)
                        Text(text = "${machine.powerConsumptionKw} kW | ${machine.durationHours}h")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen() { Box(Modifier.fillMaxSize()) { Text("Ekran Główny", modifier = Modifier.padding(16.dp)) } }

@Composable
fun SchedulerScreen(repository: MachineRepository) { Box(Modifier.fillMaxSize()) { Text("Ekran Grafiku", modifier = Modifier.padding(16.dp)) } }