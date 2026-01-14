package com.example.smartscheduler.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.ui.MachineViewModel

@SuppressLint("DefaultLocale")
@Composable
fun SchedulerScreen(viewModel: MachineViewModel) {
    val machines by viewModel.allMachines.collectAsState(initial = emptyList())
    val activeMachines = machines.filter { it.isActiveToday }
    val prices = viewModel.energyPrices
    val context = LocalContext.current

    // Używamy ceny w PLN/kWh
    val totalCost = activeMachines.sumOf { machine ->
        val startTime = machine.plannedHour
        (0 until machine.durationHours).sumOf { offset ->
            val hourData = prices.find { it.hour == (startTime + offset) % 24 }
            val priceKwh = (hourData?.price ?: 0.0) / 1000.0
            (priceKwh * machine.powerConsumptionKw)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Harmonogram Pracy", style = MaterialTheme.typography.headlineSmall)

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Przewidywany koszt całkowity", style = MaterialTheme.typography.labelLarge)
                if (prices.isEmpty()) {
                    Text("Ładowanie cen...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("${String.format("%.2f", totalCost)} PLN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (activeMachines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Brak aktywnych zadań na dziś")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activeMachines) { machine ->
                val machineCost = (0 until machine.durationHours).sumOf { offset ->
                    val hourData = prices.find { it.hour == (machine.plannedHour + offset) % 24 }
                    val priceKwh = (hourData?.price ?: 0.0) / 1000.0
                    priceKwh * machine.powerConsumptionKw
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(machine.name, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.2f", machineCost)} PLN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                " Optymalny start: ${machine.plannedHour}:00",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(
                            text = "Czas pracy: ${machine.durationHours}h | Moc: ${machine.powerConsumptionKw}kW",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.sendPlanToServer()
                // Wyświetlenie powiadomienia Toast
                android.widget.Toast.makeText(
                    context,
                    "Harmonogram został wysłany.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("WYŚLIJ PLAN DO PRODUKCJI")
        }
    }
}