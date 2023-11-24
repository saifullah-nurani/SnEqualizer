plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    namespace = "com.nurani.audio"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api("androidx.appcompat:appcompat:1.6.1")
    api("com.google.android.material:material:1.10.0")
    // vertical seekbar
    implementation("com.h6ah4i.android.widget.verticalseekbar:verticalseekbar:1.0.0")

    // datastore
    implementation("androidx.datastore:datastore:1.0.0")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
