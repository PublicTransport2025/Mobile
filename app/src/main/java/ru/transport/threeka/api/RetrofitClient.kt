package ru.transport.threeka.api

import android.content.Context
import androidx.compose.ui.res.stringResource
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.transport.threeka.R

object RetrofitClient {
    private lateinit var retrofit: Retrofit

    fun init(context: Context, token: String) {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()
        val baseUrl = context.getString(R.string.base_url)
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}


class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("api-key", token)
            .build()
        return chain.proceed(newRequest)
    }
}