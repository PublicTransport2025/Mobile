import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}



android {
    namespace = "ru.transport.threeka"
    compileSdk = 35

    fun getKeyName(): String {
        val properties = Properties()
        project.file("local.properties").inputStream().use { properties.load(it) }
        return properties.getProperty("KEY_NAME", "")
    }

    fun getKeyPass(): String {
        val properties = Properties()
        project.file("local.properties").inputStream().use { properties.load(it) }
        return properties.getProperty("KEY_PASS", "")
    }

    fun getKeyAlias(): String {
        val properties = Properties()
        project.file("local.properties").inputStream().use { properties.load(it) }
        return properties.getProperty("KEY_ALIAS", "")
    }

    signingConfigs {
        create("release") {
            storeFile = file(getKeyName()) // Укажите путь к вашему keystore файлу
            storePassword = getKeyPass() // Укажите пароль к keystore
            keyAlias = getKeyAlias() // Укажите алиас ключа
            keyPassword = getKeyPass() // Укажите пароль к ключу
        }
    }

    buildFeatures {
        buildConfig = true // Включаем поддержку пользовательских полей в BuildConfig
    }



    fun getMapkitApiKey(): String {
        val properties = Properties()
        project.file("local.properties").inputStream().use { properties.load(it) }
        return properties.getProperty("MAPKIT_API_KEY", "")
    }

    val mapkitApiKey = getMapkitApiKey()


    fun getVkClient(): String {
        val properties = Properties()
        project.file("local.properties").inputStream().use { properties.load(it) }
        return properties.getProperty("VK_CLIENT", "")
    }

    fun getVkSecret(): String {
        val properties = Properties()
        project.file("local.properties").inputStream().use { properties.load(it) }
        return properties.getProperty("VK_SECRET", "")
    }



    defaultConfig {
        buildConfigField("String", "MAPKIT_API_KEY", "\"$mapkitApiKey\"")
        applicationId = "ru.transport.threeka"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        addManifestPlaceholders(
            mapOf(
                "VKIDRedirectHost" to "vk.com", // Обычно vk.com.
                "VKIDRedirectScheme" to "vk" + getVkClient(), // Строго в формате vk{ID приложения}.
                "VKIDClientID" to getVkClient(),
                "VKIDClientSecret" to getVkSecret()
            )
        )
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.compose.material3:material3:1.3.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.yandex.android:maps.mobile:4.5.0-lite")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    implementation("com.vk.id:vkid:2.3.1")
    implementation("com.vk.id:onetap-compose:2.3.1")
    implementation("com.vk.id:onetap-xml:2.3.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}