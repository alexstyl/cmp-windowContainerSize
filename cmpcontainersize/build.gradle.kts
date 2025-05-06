@file:Suppress("UnstableApiUsage")

import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.multiplatform)
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

val publishGroupId = "com.alexstyl"
val publishVersion = "1.0.0"
val githubUrl = "github.com/alexstyl/cmpcontainersize"

java {
    toolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }

    androidTarget {
        publishLibraryVariants("release", "debug")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class) wasmJs {
        browser()
    }

    js(IR) {
        browser()
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CmpContainerSize"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.foundation)
            }
        }
        androidMain.dependencies {
            implementation(libs.window)
        }
        val nonAndroidMain by creating {
            dependsOn(commonMain)
        }
        iosMain {
            dependsOn(nonAndroidMain)
        }
        jvmMain {
            dependsOn(nonAndroidMain)
        }
        jsMain {
            dependsOn(nonAndroidMain)
        }
        wasmJsMain {
            dependsOn(nonAndroidMain)
        }
    }
}

android {
    namespace = "com.alexstyl.cmpcontainersize"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
}

val dokkaOutputDir = buildDir.resolve("dokka")
tasks.dokkaHtml { outputDirectory.set(file(dokkaOutputDir)) }
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") { delete(dokkaOutputDir) }
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    from(dokkaOutputDir)
}

group = publishGroupId
version = publishVersion


afterEvaluate {
    publishing {
        publications {
            withType<MavenPublication> {
                artifact(javadocJar)

                pom {
                    name.set("CMP Container Size")
                    description.set("A Compose Multiplatform utility that provides an observable way of knowing the size of the window you are in.")
                    url.set("https://${githubUrl}")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://${githubUrl}/blob/main/LICENSE")
                        }
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://${githubUrl}/issues")
                    }
                    developers {
                        developer {
                            id.set("composablehorizons")
                            name.set("Composable Horizons")
                            email.set("alex@composablehorizons.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:${githubUrl}.git")
                        developerConnection.set("scm:git:ssh://${githubUrl}.git")
                        url.set("https://${githubUrl}/tree/main")
                    }
                }
            }
        }
        // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
        project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
            dependsOn(project.tasks.withType(Sign::class.java))
        }
    }
}

signing {
    useInMemoryPgpKeys(
        getLocalProperty("signing.keyId") ?: System.getenv("SIGNING_KEY_ID"),
        getLocalProperty("signing.key") ?: System.getenv("SIGNING_KEY"),
        getLocalProperty("signing.password") ?: System.getenv("SIGNING_PASSWORD"),
    )
    sign(publishing.publications)
}
