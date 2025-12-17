package com.example.smartscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smartscheduler.data.*
import com.example.smartscheduler.ui.AppNavigation
import com.example.smartscheduler.ui.MachineViewModel
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = MachineRepository(database.machineDao())

        val viewModel: MachineViewModel by viewModels {
            MachineViewModel.Factory(repository)
        }

        setContent {
            SmartSchedulerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}