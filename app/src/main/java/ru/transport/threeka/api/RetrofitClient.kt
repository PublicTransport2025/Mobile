package ru.transport.threeka.api

import android.content.Context
import androidx.compose.ui.res.stringResource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.transport.threeka.R

object RetrofitClient {
    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        val baseUrl = context.getString(R.string.base_url)
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}