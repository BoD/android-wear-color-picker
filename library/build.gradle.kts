import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig

plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("com.jfrog.bintray")
}

group = "org.jraf"
version = "2.2.3"
description = "android-wear-color-picker"

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(30)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(kotlin("stdlib", Versions.KOTLIN))
    implementation("androidx.wear", "wear", Versions.ANDROIDX_WEAR)
}

afterEvaluate {
    android.libraryVariants.forEach { variant ->
        task<Jar>("jar${variant.name.capitalize()}Sources") {
            description = "Generate a sources Jar for ${variant.name}."
            group = "Publishing"
            archiveClassifier.set("sources")
            from(variant.sourceSets.map { it.javaDirectories })
        }
    }

    publishing {
        publications {
            create<MavenPublication>("releaseMavenPublication") {
                from(components["release"])
                artifactId = description
                artifact(tasks["jarReleaseSources"])
            }
        }
    }

    bintray {
        user = System.getenv("USER")
        key = System.getenv("KEY")
        setPublications("releaseMavenPublication")
        pkg(delegateClosureOf<PackageConfig> {
            repo = "JRAF"
            name = "android-wear-color-picker"
            userOrg = "bod"
            setLicenses("Apache-2.0")
            vcsUrl = "https://github.com/BoD/android-wear-color-picker"
            version(delegateClosureOf<VersionConfig> {
                name = project.version.toString()
            })
        })
        publish = true
    }
}

// Use "./gradlew publishToMavenLocal" to deploy the artifacts to your local maven repository
// Use "USER=<username> KEY=<key> ./gradlew publishToMavenLocal bintrayUpload" to deploy the artifacts to bintray
// key can be found here: https://bintray.com/profile/edit and click on "API key"
