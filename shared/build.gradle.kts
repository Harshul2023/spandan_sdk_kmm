import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id ("kotlinx-serialization")

}
project.afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.example.spandan_sdk"
                artifactId = "spandan_sdk"
                version = "1.0.0"
                from(components["kotlin"])
            }
        }
        repositories {
            mavenLocal()
        }
    }
}
kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
    binaries.all {
        freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
    }
}
repositories {

    // Load properties from a file
    val localProperties = Properties()
    localProperties.load(File("local.properties").inputStream())
    val properties = Properties()
    val localPropertiesFile = File(rootProject.rootDir, "local.properties")
    if (localPropertiesFile.exists()) { properties.load(FileInputStream(localPropertiesFile)) }

    mavenLocal()
    google()
    mavenCentral()
//    maven { url "https://plugins.gradle.org/m2/" }
    maven {
        url = uri("https://maven.pkg.github.com/sunfox-technologies/sericom")
        credentials {
            username = properties.getProperty("repo.username", "")
            password = properties.getProperty("repo.token", "")
        }
    }
}

kotlin {
    androidTarget {
        publishAllLibraryVariants()
        publishLibraryVariantsGroupedByFlavor = true
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "13.0"
        framework {
            baseName = "shared"
            isStatic = true
        }
        pod("SericomPod") {
            val localProperties = Properties()
            localProperties.load(File("local.properties").inputStream())
            val properties = Properties()
            val localPropertiesFile = File(rootProject.rootDir, "local.properties")
            if (localPropertiesFile.exists()) { properties.load(FileInputStream(localPropertiesFile)) }

            source = git("https://username:"+properties.getProperty("cocoapod.token","")+"@github.com/sunfox-technologies/sericomm_ios.git/") {
                branch = "encryption2"
            }

            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.example.ecg_processor_kmm:ecg_processor_kmm:1.0.1")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            implementation("com.benasher44:uuid:0.8.4")
            implementation("io.ktor:ktor-client-core:2.3.11")
            implementation("io.ktor:ktor-client-json:2.3.11")
            implementation("io.ktor:ktor-client-serialization:2.3.11")
            implementation("io.ktor:ktor-server-content-negotiation:2.3.11")
            implementation("de.peilicke.sascha:kase64:1.2.0")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
            implementation("io.ktor:ktor-client-logging:2.3.11")
            implementation("com.squareup.okio:okio:3.9.0")

            val core = "0.5.1"
            implementation("org.kotlincrypto.core:digest:$core")
            implementation("org.kotlincrypto.core:mac:$core")
            implementation("org.kotlincrypto.core:xof:$core")
            implementation("com.squareup.okio:okio:3.0.0")


        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.11")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation ("in.sunfox.healthcare.commons.android.sericom:sericom:1.0.8")

            }
        }
        val iosMain by creating {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.11")
            }
        }


    }
}

android {
    namespace = "com.example.spandan_sdk_kotlin"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    releaseImplementation(libs.ecg.processor.kmm)
}
