package ru.transport.threeka

import android.app.Application
import com.vk.id.VKID
import com.yandex.mapkit.MapKitFactory
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.transport.threeka.api.RetrofitClient
import ru.transport.threeka.services.BusAlertManager
import java.util.Locale

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = AppMetricaConfig.newConfigBuilder(BuildConfig.APPMETRIKA_API_KEY).build()
        AppMetrica.activate(this, config)
        RetrofitClient.init(this, BuildConfig.SERVER_API_KEY)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        VKID.init(this)
        VKID.instance.setLocale(Locale("ru"))
        VKID.logsEnabled = true

        val busAlertManager = BusAlertManager(this)
        busAlertManager.createNotificationChannel()
    }
}