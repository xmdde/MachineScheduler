package com.example.smartscheduler.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val powerConsumptionKw: Double,
    val durationHours: Int,
    val priority: Int,
    val isActiveToday: Boolean = false
)