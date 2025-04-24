package ru.transport.threeka.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.api.schemas.navigation.RouteReport

interface ApiService {
    @GET("/api/stops")
    fun getStops(): Call<List<Stop>>

    @GET("/api/navigation")
    fun createRoute(
        @Query("from_id") fromId: Int,
        @Query("to_id") toId: Int
    ): Call<RouteReport>
}