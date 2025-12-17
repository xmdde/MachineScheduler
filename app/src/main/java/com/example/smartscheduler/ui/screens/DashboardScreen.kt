package com.example.smartscheduler.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartscheduler.data.MockEnergyApi
import com.example.smartscheduler.ui.MachineViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: MachineViewModel) {
    val machines by viewModel.allMachines.collectAsState(initial = emptyList())
    val prices = MockEnergyApi.getPrices()
    val maxPrice = prices.maxOf { it.price }

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl", "PL")))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Centrum Dowodzenia", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("CENY ENERGII: $currentDate", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

        Card(modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                prices.forEach { energy ->
                    val barHeight = (energy.price / maxPrice).toFloat()
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(barHeight).padding(horizontal = 1.dp).background(color = if (energy.price > 0.9) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Plan zadań na dziś:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(machines) { machine ->
                ListItem(
                    headlineContent = { Text(machine.name, fontWeight = FontWeight.SemiBold) },
                    leadingContent = {
                        Checkbox(checked = machine.isActiveToday, onCheckedChange = { viewModel.toggleActive(machine, it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = if (machine.isActiveToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent)
                )
            }
        }
    }
}