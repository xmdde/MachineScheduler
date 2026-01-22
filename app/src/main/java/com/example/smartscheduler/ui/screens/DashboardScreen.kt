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
import com.example.smartscheduler.ui.MachineViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: MachineViewModel) {
    val machines by viewModel.allMachines.collectAsState(initial = emptyList())

    val prices = viewModel.energyPrices

    val maxPrice = if (prices.isNotEmpty()) prices.maxOf { it.price } else 1000.0

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl", "PL")))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Centrum Dowodzenia", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text("CENY ENERGII: $currentDate", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

        val lowestPrice = prices.minOfOrNull { it.price } ?: 0.0

        // Wykres cen energii w danym dniu.
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    prices.forEach { energy ->
                        val barHeight = (energy.price / maxPrice).toFloat()

                        val barColor = when {
                            energy.price == lowestPrice -> Color(0xFF4CAF50)
                            energy.price > 600.0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(barHeight.coerceIn(0.05f, 1f))
                                    .padding(horizontal = 2.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(barColor, barColor.copy(alpha = 0.6f))
                                        ),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }

                // Oś godzinowa
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    for (h in 0..23) {
                        if (h % 3 == 0) {
                            Text(
                                text = "$h",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Plan zadań na dziś:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(machines) { machine ->
                ListItem(
                    headlineContent = { Text(machine.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text("Zalecana godzina startu: ${machine.plannedHour}:00")
                    },
                    leadingContent = {
                        Checkbox(checked = machine.isActiveToday, onCheckedChange = { viewModel.toggleActive(machine, it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = if (machine.isActiveToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent)
                )
            }
        }
    }
}