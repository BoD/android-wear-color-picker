plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("org.jetbrains.dokka") version Versions.DOKKA_PLUGIN
    id("signing")
}

group = "org.jraf"
version = "2.2.4"
description = "android-wear-color-picker"

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 23
        targetSdk = 31
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

        task<Jar>("jar${variant.name.capitalize()}DokkaHtml") {
            description = "Generate a javadoc (Dokka html) Jar for ${variant.name}."
            group = "Publishing"
            archiveClassifier.set("javadoc")
            from("$buildDir/dokka")
            dependsOn("dokkaHtml")
        }
    }

    publishing {
        repositories {
            maven {
                // Note: declare your user name / password in your home's gradle.properties like this:
                // mavenCentralNexusUsername = <user name>
                // mavenCentralNexusPassword = <password>
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                name = "mavenCentralNexus"
                credentials(PasswordCredentials::class)
            }
        }

        publications {
            create<MavenPublication>("releaseMavenPublication") {
                from(components["release"])
                artifactId = description
                artifact(tasks["jarReleaseSources"])
                artifact(tasks["jarReleaseDokkaHtml"])

                pom {
                    name.set("android-wear-color-picker")
                    description.set("A color picker activity optimized for Wear OS (aka Android Wear). Handy for watch face settings.")
                    url.set("https://github.com/BoD/android-wear-color-picker")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("BoD")
                            name.set("Benoit 'BoD' Lubek")
                            email.set("BoD@JRAF.org")
                            url.set("https://JRAF.org")
                            organization.set("JRAF.org")
                            organizationUrl.set("https://JRAF.org")
                            roles.set(listOf("developer"))
                            timezone.set("+1")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/BoD/android-wear-color-picker")
                        developerConnection.set("scm:git:https://github.com/BoD/android-wear-color-picker")
                        url.set("https://github.com/BoD/android-wear-color-picker")
                    }
                    issueManagement {
                        url.set("https://github.com/BoD/android-wear-color-picker/issues")
                        system.set("GitHub Issues")
                    }
                }
            }
        }
    }

    signing {
        // Note: declare the signature key, password and file in your home's gradle.properties like this:
        // signing.keyId=<8 character key>
        // signing.password=<your password>
        // signing.secretKeyRingFile=<absolute path to the gpg private key>
        sign(publishing.publications)
    }
}

// Run `./gradlew publishToMavenLocal` to publish to the local maven repo
// Run `./gradlew publish` to publish to Maven Central (then go to https://oss.sonatype.org/#stagingRepositories and "close", and "release")
