package ru.transport.threeka.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.transport.threeka.api.schemas.ResponseData
import ru.transport.threeka.api.schemas.Route
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.api.schemas.VKLogin

interface ApiService {
    @GET("/api/stops")
    fun getStops(): Call<List<Stop>>

    @GET("/route")
    fun getRoute(): Call<Route>
}