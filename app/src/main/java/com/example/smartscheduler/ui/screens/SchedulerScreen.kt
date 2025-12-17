package com.example.smartscheduler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.data.MockEnergyApi
import com.example.smartscheduler.calculateOptimalStartTime
import com.example.smartscheduler.ui.MachineViewModel

@Composable
fun SchedulerScreen(viewModel: MachineViewModel) {
    val machines by viewModel.allMachines.collectAsState(initial = emptyList())
    val activeMachines = machines.filter { it.isActiveToday }
    val prices = MockEnergyApi.getPrices()

    val totalCost = activeMachines.sumOf { machine ->
        val startTime = calculateOptimalStartTime(machine.durationHours, prices)
        (0 until machine.durationHours).sumOf { offset ->
            prices[(startTime + offset) % 24].price * machine.powerConsumptionKw
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Harmonogram Pracy", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Przewidywany koszt całkowity", style = MaterialTheme.typography.labelLarge)
                Text("${String.format("%.2f", totalCost)} PLN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activeMachines) { machine ->
                val startTime = calculateOptimalStartTime(machine.durationHours, prices)
                val cost = (0 until machine.durationHours).sumOf { offset -> prices[(startTime + offset) % 24].price * machine.powerConsumptionKw }

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(machine.name, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.2f", cost)} PLN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                            Text(" Sugerowany start: $startTime:00", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}