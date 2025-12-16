package com.example.smartscheduler.data

data class EnergyPrice(val hour: Int, val price: Double)

object MockEnergyApi {
    fun getPrices(): List<EnergyPrice> {
        return (0..23).map { hour ->
            val price = when(hour) {
                in 9..17 -> 1.20
                else -> 0.40
            }
            EnergyPrice(hour, price)
        }
    }
}