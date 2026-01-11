package com.example.smartscheduler.data

import kotlinx.coroutines.flow.Flow

class MachineRepository(private val machineDao: MachineDao) {
    val allMachines: Flow<List<MachineEntity>> = machineDao.getAllMachines()

    suspend fun addMachine(machine: MachineEntity) {
        machineDao.insertMachine(machine)
    }

    suspend fun deleteMachine(machine: MachineEntity) {
        machineDao.deleteMachine(machine)
    }

    suspend fun update(machine: MachineEntity) {
        machineDao.update(machine)
    }
}