package com.example.smartscheduler

import com.example.smartscheduler.data.EnergyPriceDto
import com.example.smartscheduler.data.MachinePlanDto
import retrofit2.Response
import retrofit2.http.*
import retrofit2.converter.gson.GsonConverterFactory

interface SmartSchedulerApi {
    @GET("energy/prices")
    suspend fun getPrices(@Query("date") date: String): List<EnergyPriceDto>

    @POST("machine/schedule")
    suspend fun sendSchedule(@Body schedule: MachinePlanDto): Response<Unit>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000/"

        fun create(): SmartSchedulerApi {
            return retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SmartSchedulerApi::class.java)
        }
    }
}