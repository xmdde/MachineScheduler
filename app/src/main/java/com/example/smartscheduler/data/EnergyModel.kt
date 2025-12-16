package com.example.smartscheduler.data

data class EnergyPrice(val hour: Int, val price: Double)

object MockEnergyApi {
    fun getPrices(): List<EnergyPrice> {
        return (0..23).map { hour ->
            val price = when (hour) {
                in 0..5 -> 0.35
                in 6..8 -> 0.90
                in 9..15 -> 0.70
                in 16..20 -> 1.35
                else -> 0.55
            }
            EnergyPrice(hour, price)
        }
    }
}