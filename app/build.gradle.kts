plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "vn.viettube.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.viettube.app"
        minSdk = 26
        targetSdk = 34
        versionCode = buildNumber
        versionName = "1.2.$buildNumber"
    }

    buildTypes {
        getByName("debug") {
            // Independent package; safe to install next to stable and parked-video beta.
            applicationIdSuffix = ".weblink"
            versionNameSuffix = "-weblink-beta"
        }
        getByName("release") {
            // Deliberately unsigned: rotate the previously exposed signing key
            // before any production release. This branch distributes debug only.
            isMinifyEnabled = false
        }
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
    implementation("androidx.core:core-ktx:1.13.1")
}
