@file:SuppressLint("UseTomlInstead")

import android.annotation.SuppressLint

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "org.jraf.android.androidwearcolorpicker.sample"
    compileSdk = 33

    defaultConfig {
        applicationId = "org.jraf.android.androidwearcolorpicker.sample"
        minSdk = 25
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // See https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    // See https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(8)
}

dependencies {
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.activity.compose)

    implementation(project(":library"))
}
