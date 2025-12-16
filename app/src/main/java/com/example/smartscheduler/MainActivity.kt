package com.example.smartscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartscheduler.data.*
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = MachineRepository(database.machineDao())

        setContent {
            SmartSchedulerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(repository)
                }
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
                    onClick = { navController.navigate(Screen.Dashboard.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Główna") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.MachineList.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Maszyny") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Scheduler.route) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Grafik") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MachineList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(repository) }
            composable(Screen.MachineList.route) { MainScreen(repository) }
            composable(Screen.Scheduler.route) { SchedulerScreen(repository) }
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
    var editingMachine by remember { mutableStateOf<MachineEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (editingMachine == null) "Dodaj Maszynę" else "Edytuj Maszynę", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = power, onValueChange = { power = it }, label = { Text("Moc (kW)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Czas (h)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    val machineToSave = MachineEntity(
                        id = editingMachine?.id ?: 0,
                        name = name,
                        powerConsumptionKw = power.toDoubleOrNull() ?: 0.0,
                        durationHours = duration.toIntOrNull() ?: 1,
                        priority = 1
                    )
                    repository.addMachine(machineToSave)
                    name = ""; power = ""; duration = ""; editingMachine = null
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text(if (editingMachine == null) "Dodaj" else "Zapisz zmiany")
        }

        LazyColumn {
            items(machines) { machine ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = machine.name, fontWeight = FontWeight.Bold)
                            Text(text = "${machine.powerConsumptionKw} kW | ${machine.durationHours}h")
                        }

                        IconButton(onClick = {
                            editingMachine = machine
                            name = machine.name
                            power = machine.powerConsumptionKw.toString()
                            duration = machine.durationHours.toString()
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }

                        IconButton(onClick = { scope.launch { repository.deleteMachine(machine) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SchedulerScreen(repository: MachineRepository) {
    val machines by repository.allMachines.collectAsState(initial = emptyList())
    val activeMachines = machines.filter { it.isActiveToday }
    val prices = MockEnergyApi.getPrices()

    val totalCost = activeMachines.sumOf { machine ->
        val startTime = calculateOptimalStartTime(machine.durationHours, prices)
        (0 until machine.durationHours).sumOf { offset ->
            val currentHour = (startTime + offset) % 24
            prices[currentHour].price * machine.powerConsumptionKw
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Harmonogram Pracy", style = MaterialTheme.typography.headlineSmall)

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Przewidywany całkowity koszt energii", style = MaterialTheme.typography.labelLarge)
                Text("${String.format("%.2f", totalCost)} PLN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold)
            }
        }

        if (activeMachines.isEmpty()) {
            Text("Nie wybrano aktywnych maszyn na dziś.")
        } else {
            Text("Szczegóły maszyn:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activeMachines) { machine ->
                    val startTime = calculateOptimalStartTime(machine.durationHours, prices)
                    val endTime = (startTime + machine.durationHours) % 24

                    val cost = (0 until machine.durationHours).sumOf { offset ->
                        val hour = (startTime + offset) % 24
                        prices[hour].price * machine.powerConsumptionKw
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(machine.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${String.format("%.2f", cost)} PLN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sugerowany czas pracy: ", style = MaterialTheme.typography.bodyMedium)
                                Text("$startTime:00 - $endTime:00", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                "Zużycie: ${machine.powerConsumptionKw}kW x ${machine.durationHours}h",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(repository: MachineRepository) {
    val scope = rememberCoroutineScope()

    val machines by repository.allMachines.collectAsState(initial = emptyList())
    val prices = MockEnergyApi.getPrices()
    val maxPrice = prices.maxOf { it.price }

    val currentDate = LocalDate.now().format(
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl", "PL"))
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Centrum Dowodzenia", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CENY ENERGII: $currentDate",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                prices.forEach { energy ->
                    val barHeight = (energy.price / maxPrice).toFloat()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(barHeight)
                            .padding(horizontal = 1.dp)
                            .background(
                                color = if (energy.price > 0.9) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Plan zadań na dziś (wybierz do grafiku):", style = MaterialTheme.typography.titleMedium)

        if (machines.isEmpty()) {
            Text("Brak maszyn w bazie.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(machines) { machine ->
                    ListItem(
                        headlineContent = { Text(machine.name, fontWeight = FontWeight.SemiBold) },
                        leadingContent = {
                            Checkbox(
                                checked = machine.isActiveToday,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        repository.addMachine(machine.copy(isActiveToday = checked))
                                    }
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (machine.isActiveToday)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}