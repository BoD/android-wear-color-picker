import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions") version Versions.BEN_MANES_VERSIONS_PLUGIN
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build", "gradle", Versions.ANDROID_GRADLE_PLUGIN)
        classpath(kotlin("gradle-plugin", Versions.KOTLIN))
        classpath("com.jfrog.bintray.gradle", "gradle-bintray-plugin", Versions.BINTRAY_PUBLISH_PLUGIN)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    // Show a report in the log when running tests
    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }
    }
}

tasks {
    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = Versions.GRADLE
    }

    // Configuration for gradle-versions-plugin
    // Run `./gradlew dependencyUpdates` to see latest versions of dependencies
    withType<DependencyUpdatesTask> {
        resolutionStrategy {
            componentSelection {
                all {
                    if (setOf("alpha", "beta", "rc", "preview", "eap", "m1").any { candidate.version.contains(it, true) }) {
                        reject("Non stable")
                    }
                }
            }
        }
    }
}
