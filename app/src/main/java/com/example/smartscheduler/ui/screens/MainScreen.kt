package com.example.smartscheduler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.data.MachineEntity
import com.example.smartscheduler.ui.MachineViewModel

@Composable
fun MainScreen(viewModel: MachineViewModel) {
    val machines by viewModel.allMachines.collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var power by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var editingMachine by remember { mutableStateOf<MachineEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (editingMachine == null) "Dodaj Maszynę" else "Edytuj Maszynę", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = power, onValueChange = { power = it }, label = { Text("Moc (kW)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Czas (h)") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            val machineToSave = MachineEntity(
                id = editingMachine?.id ?: 0,
                name = name,
                powerConsumptionKw = power.toDoubleOrNull() ?: 0.0,
                durationHours = duration.toIntOrNull() ?: 1,
                priority = 1,
                isActiveToday = editingMachine?.isActiveToday ?: false
            )
            viewModel.saveMachine(machineToSave)
            name = ""; power = ""; duration = ""; editingMachine = null
        }, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(if (editingMachine == null) "Dodaj" else "Zapisz")
        }

        LazyColumn {
            items(machines) { machine ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(machine.name, fontWeight = FontWeight.Bold)
                            Text("${machine.powerConsumptionKw} kW | ${machine.durationHours}h")
                        }
                        IconButton(onClick = {
                            editingMachine = machine
                            name = machine.name; power = machine.powerConsumptionKw.toString(); duration = machine.durationHours.toString()
                        }) { Icon(Icons.Default.Edit, null) }
                        IconButton(onClick = { viewModel.deleteMachine(machine) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}