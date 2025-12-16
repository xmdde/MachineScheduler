package com.example.smartscheduler

import com.example.smartscheduler.data.EnergyPrice

fun calculateOptimalStartTime(duration: Int, prices: List<EnergyPrice>): Int {
    if (duration <= 0 || duration >= 24) return 0

    val bestWindow = prices.windowed(size = duration, step = 1)
        .minByOrNull { window -> window.sumOf { it.price } }

    return bestWindow?.firstOrNull()?.hour ?: 0
}