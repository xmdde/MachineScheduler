package com.example.smartscheduler

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object MachineList : Screen("machines")
    object Scheduler : Screen("scheduler")
}