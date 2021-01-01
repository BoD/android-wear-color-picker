plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "org.jraf.android.androidwearcolorpicker.sample"
        minSdkVersion(23)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib", Versions.KOTLIN))
    implementation("com.google.android.support", "wearable", Versions.GOOGLE_SUPPORT_WEARABLE)
    implementation("com.google.android.gms", "play-services-wearable", Versions.PLAY_SERVICE_WEARABLE)
    implementation("androidx.wear", "wear", Versions.ANDROIDX_WEAR)
    implementation("androidx.constraintlayout", "constraintlayout", Versions.ANDROIDX_CONSTRAINT_LAYOUT)
    compileOnly("com.google.android.wearable", "wearable", Versions.GOOGLE_WEARABLE)

    implementation(project(":library"))
}
