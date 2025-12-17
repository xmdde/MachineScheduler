package com.example.smartscheduler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.data.MachineEntity
import com.example.smartscheduler.data.MachineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
    class Factory(private val repository: MachineRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MachineViewModel(repository) as T
        }
    }
}