package ru.transport.threeka

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import ru.transport.threeka.api.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}