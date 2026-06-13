plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.en.cyrisia"
    compileSdk = 34

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.extension.en.cyrisia"
        minSdk = 29
        targetSdk = 34
        // Suwayomi parses the index `version` (e.g. "1.4.1") as the lib version and
        // requires it in [1.3, 1.5]; keep the APK versionName aligned with that.
        versionCode = 1
        versionName = "1.4.1"
    }

    base {
        archivesName.set("tachiyomi-en.cyrisia-v1.4.1")
    }

    // The Tachiyomi/Suwayomi runtime provides these libraries; do not bundle them.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    compileOnly("com.github.keiyoushi:extensions-lib:18a8e26be2")
    compileOnly("com.squareup.okhttp3:okhttp:5.3.2")
    compileOnly("org.jsoup:jsoup:1.22.1")
    compileOnly("io.reactivex:rxjava:1.3.8")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
}
