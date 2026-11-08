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
        versionName = "1.1.$buildNumber"
    }

    // Legacy signing retained for local beta updates ONLY.
    // The keystore is publicly committed and MUST NOT be used for production.
    signingConfigs {
        create("viettube") {
            storeFile = file("viettube.keystore")
            storePassword = "viettube"
            keyAlias = "viettube"
            keyPassword = "viettube"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("viettube")
        }
        getByName("debug") {
            // Install beta beside the original app; never replace a working APK.
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
            signingConfig = signingConfigs.getByName("viettube")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
