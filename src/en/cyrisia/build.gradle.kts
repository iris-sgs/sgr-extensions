plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.en.cyrisia"
    compileSdk = 34

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.extension.en.cyrisia"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.4.1"
    }

    base {
        archivesName.set("tachiyomi-en.cyrisia-v1.4.1")
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    compileOnly("com.github.keiyoushi:extensions-lib:18a8e26be2")
    compileOnly("com.squareup.okhttp3:okhttp:5.3.2")
    compileOnly("org.jsoup:jsoup:1.22.1")
    compileOnly("io.reactivex:rxjava:1.3.8")
}
