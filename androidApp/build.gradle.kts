import com.android.build.api.dsl.ApkSigningConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":sharedLogic"))

    // Logging
    implementation(libs.timber)

    // Вход через Яндекс
    implementation(libs.yandex.authsdk)

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Навигация
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Базовые библиотеки Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (Нативный, через BOM)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Tooling для превью
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}

android {
    namespace = "com.command.polyshelf"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.command.polyshelf"
        // ID приложения из https://oauth.yandex.ru. Не секрет, но зависит от окружения:
        // CI передаёт его переменной YANDEX_CLIENT_ID, локально можно положить в ~/.gradle/gradle.properties.
        manifestPlaceholders["YANDEX_CLIENT_ID"] = System.getenv("YANDEX_CLIENT_ID")
            ?: (project.findProperty("YANDEX_CLIENT_ID") as String?)
            ?: ""
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Ключи подписи лежат в секретах GitHub и приходят в CI переменными окружения:
    //   <PREFIX>_KEYSTORE_PATH, <PREFIX>_KEYSTORE_PASSWORD, <PREFIX>_KEY_ALIAS, <PREFIX>_KEY_PASSWORD,
    // где PREFIX = PRE_RELEASE или RELEASE. Постоянный ключ => постоянный SHA256-отпечаток для Яндекса.
    // Без переменных (локальная сборка) возвращается null и берётся debug-ключ.
    fun signingFor(prefix: String): ApkSigningConfig? {
        val path = System.getenv("${prefix}_KEYSTORE_PATH")
        return if (path == null) {
            null
        } else {
            signingConfigs.create(prefix.lowercase()) {
                storeFile = file(path)
                storePassword = System.getenv("${prefix}_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("${prefix}_KEY_ALIAS")
                keyPassword = System.getenv("${prefix}_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingFor("RELEASE")
        }
        // Пре-релизная сборка: как release, но подписана ключом PRE_RELEASE.
        create("preRelease") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            signingConfig = signingFor("PRE_RELEASE") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
