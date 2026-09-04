import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

/**
 * Release signing, read from `keystore.properties` at the repo root. Both that file and the
 * keystore it points at are gitignored, so a clone without them still builds — it just falls back
 * to an unsigned release, and `assembleDebug` is unaffected.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

/**
 * Receipt scanning talks to OpenRouter, and the key that pays for it is nobody's business but the
 * person who built the APK. It is read from `local.properties` — gitignored, and already the file
 * the SDK path lives in — so a clone without one still builds; the app then reports the key as
 * missing rather than firing a request that would be rejected anyway.
 *
 *     openrouter.apiKey=sk-or-v1-...
 *     openrouter.model=google/gemini-3.8-flash
 */
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun localProperty(key: String, fallback: String): String =
    (localProperties.getProperty(key) ?: fallback).trim()

android {
    namespace = "com.yudha.catatanbelanja"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.yudha.catatanbelanja"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField(
            "String",
            "OPENROUTER_API_KEY",
            "\"" + localProperty("openrouter.apiKey", "<API_KEY>") + "\"",
        )
        buildConfigField(
            "String",
            "OPENROUTER_MODEL",
            "\"" + localProperty("openrouter.model", "google/gemini-3.8-flash") + "\"",
        )
    }

    signingConfigs {
        if (!keystoreProperties.isEmpty) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    sourceSets["main"].java.srcDirs("src/main/kotlin")
}

dependencies {
    implementation(project(":shared"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
