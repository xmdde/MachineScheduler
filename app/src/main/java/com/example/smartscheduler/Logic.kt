package com.example.smartscheduler

import com.example.smartscheduler.data.EnergyPriceDto

fun calculateOptimalStartTime(duration: Int, prices: List<EnergyPriceDto>): Int {
    if (duration <= 0 || duration > prices.size) return 0

    val bestWindow = prices.windowed(size = duration, step = 1)
        .minByOrNull { window -> window.sumOf { it.price } }

    return bestWindow?.firstOrNull()?.hour ?: 0
}