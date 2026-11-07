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
        versionName = "1.0.$buildNumber"
    }

    // Fixed key so each new build installs over the old one.
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
    implementation("androidx.car.app:app:1.4.0")
    implementation("androidx.car.app:app-projected:1.4.0")
    implementation("androidx.core:core-ktx:1.13.1")
}
