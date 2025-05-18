package ru.transport.threeka.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.transport.threeka.api.schemas.Atp
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.api.schemas.navigation.RouteReport

interface ApiService {
    @GET("/api/stops")
    fun getStops(): Call<List<Stop>>

    @GET("/api/navigation")
    fun createRoute(
        @Query("from_id") fromId: Int,
        @Query("to_id") toId: Int,
        @Query("care") care: Boolean,
        @Query("change") change: Boolean,
        @Query("priority") priority: Int
    ): Call<RouteReport>

    @GET("/api/atp")
    fun getAtp(
        @Query("number") number: String
    ): Call<Atp>
}