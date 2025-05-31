package ru.transport.threeka.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import ru.transport.threeka.api.schemas.Atp
import ru.transport.threeka.api.schemas.Reg
import ru.transport.threeka.api.schemas.ResetPass
import ru.transport.threeka.api.schemas.ResponseMessage
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.api.schemas.Token
import ru.transport.threeka.api.schemas.VKLogin
import ru.transport.threeka.api.schemas.navigation.Feedback
import ru.transport.threeka.api.schemas.navigation.RouteReport

interface ApiService {
    @GET("/api/stops/all")
    fun getStops(
        @Header("token") token: String?
    ): Call<MutableList<Stop>>

    @POST("/api/stops/like")
    fun likeStop(
        @Header("token") token: String,
        @Query("stop_id") stopId: Int
    ): Call<Stop>

    @DELETE("/api/stops/dislike")
    fun dislikeStop(
        @Header("token") token: String,
        @Query("stop_id") stopId: Int
    ): Call<Stop>

    @GET("/api/navigation")
    fun createRoute(
        @Query("from_id") fromId: Int,
        @Query("to_id") toId: Int,
        @Query("care") care: Boolean,
        @Query("change") change: Boolean,
        @Query("priority") priority: Int,
        @Query("time") time: Int?
    ): Call<RouteReport>

    @GET("/api/atp")
    fun getAtp(
        @Query("number") number: String
    ): Call<Atp>

    @POST("/api/auth/loginvk")
    fun loginWithVK(@Body vkLogin: VKLogin): Call<Token>

    @FormUrlEncoded
    @POST("/api/auth/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<Token>

    @POST("/api/auth/refresh")
    fun refreshToken(
        @Header("token") refresh: String
    ): Call<Token>

    @POST("/api/auth/signup")
    fun signup(@Body reg: Reg): Call<Token>

    @POST("/api/email/send-code")
    fun getCode(
        @Query("email") email: String
    ): Call<ResponseMessage>

    @POST("/api/auth/reset-password")
    fun resetPass(@Body data: ResetPass): Call<Token>

    @POST("/api/email/resend-code")
    fun getResetCode(
        @Query("email") email: String
    ): Call<ResponseMessage>

    @POST("/api/feedback/write")
    fun writeFeedback(
        @Header("token") token: String,
        @Body feedback: Feedback
    ): Call<ResponseMessage>
}