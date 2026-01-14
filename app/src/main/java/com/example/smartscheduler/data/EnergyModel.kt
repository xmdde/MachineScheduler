package com.example.smartscheduler.data

data class EnergyPriceDto(
    val hour: Int,
    val price: Double // cena w PLN/MWh
)

data class MachinePlanDto(
    val machineId: Int,
    val plannedHour: Int
)