package com.example.smartscheduler.ui

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.SmartSchedulerApi
import com.example.smartscheduler.data.EnergyPriceDto
import com.example.smartscheduler.data.MachineEntity
import com.example.smartscheduler.data.MachineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.smartscheduler.calculateOptimalStartTime
import com.example.smartscheduler.data.MachinePlanDto
import kotlinx.coroutines.flow.first

class MachineViewModel(private val repository: MachineRepository) : ViewModel() {
    val allMachines: Flow<List<MachineEntity>> = repository.allMachines

    fun saveMachine(machine: MachineEntity) {
        viewModelScope.launch {
            repository.addMachine(machine)
        }
    }

    fun deleteMachine(machine: MachineEntity) {
        viewModelScope.launch {
            repository.deleteMachine(machine)
        }
    }

    fun toggleActive(machine: MachineEntity, isActive: Boolean) {
        viewModelScope.launch {
            repository.addMachine(machine.copy(isActiveToday = isActive))
        }
    }

    private val api = SmartSchedulerApi.create()

    var energyPrices by mutableStateOf<List<EnergyPriceDto>>(emptyList())
        private set

    var isLoadingPrices by mutableStateOf(false)
        private set

    fun fetchEnergyPrices(date: String) {
        viewModelScope.launch {
            isLoadingPrices = true
            Log.d("SMART_DEBUG", "Rozpoczynam pobieranie danych dla daty: $date")

            try {
                val response = api.getPrices(date)
                energyPrices = response
                recalculateSchedule()
            } catch (e: Exception) {
                Log.e("MachineViewModel", "Błąd pobierania cen: ${e.message}")
            } finally {
                isLoadingPrices = false
                Log.d("SMART_DEBUG", "Zakończono proces pobierania.")
            }
        }
    }

    fun recalculateSchedule() {
        val prices = energyPrices
        if (prices.isEmpty()) return

        viewModelScope.launch {
            val allMachines = repository.allMachines.first()
            Log.d("SCHEDULER_CHECK", "Znaleziono ${allMachines.size} maszyn do przeliczenia")

            allMachines.forEach { machine ->
                if (machine.isActiveToday) {
                    val bestStartHour = calculateOptimalStartTime(machine.durationHours, prices)

                    Log.d("SCHEDULER_CHECK", "Maszyna: ${machine.name} (id: ${machine.id}) -> Nowa godzina: $bestStartHour")

                    val updatedMachine = machine.copy(plannedHour = bestStartHour)
                    repository.update(updatedMachine)
                }
            }
        }
    }

    fun sendPlanToServer() {
        viewModelScope.launch {
            try {
                val machines = repository.allMachines.first().filter { it.isActiveToday }

                machines.forEach { machine ->
                    val request = MachinePlanDto(
                        machineId = machine.id,
                        plannedHour = machine.plannedHour
                    )
                    val response = api.sendSchedule(request)

                    if (response.isSuccessful) {
                        Log.d("POST_API", "Wysłano harmonogram dla: ${machine.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("POST_API", "Błąd wysyłania: ${e.message}")
            }
        }
    }

    class Factory(private val repository: MachineRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MachineViewModel(repository) as T
        }
    }
}