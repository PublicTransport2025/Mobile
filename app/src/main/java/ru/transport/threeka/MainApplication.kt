package ru.transport.threeka

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.yandex.mapkit.MapKitFactory
import ru.transport.threeka.api.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this, BuildConfig.SERVER_API_KEY)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}