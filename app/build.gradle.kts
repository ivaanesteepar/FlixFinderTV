import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    kotlin("kapt")
    id("jacoco")
}

android {
    namespace = "com.example.flixfindertv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.flixfindertv"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Cargar el archivo local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")

        // Si el archivo existe, cargarlo
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // Obtener las propiedades del archivo local.properties
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val imgurClientId = localProperties.getProperty("IMGUR_CLIENT_ID") ?: ""

        android {
            defaultConfig {
                // Pasar las claves a BuildConfig
                buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
                buildConfigField("String", "IMGUR_CLIENT_ID", "\"$imgurClientId\"")
            }
        }


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true // cobertura para tests unitarios
            enableAndroidTestCoverage = true // cobertura para tests instrumentados


        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packagingOptions {
        exclude("META-INF/INDEX.LIST")
    }
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    group = "Reporting"
    description = "Generates Jacoco coverage reports for the debug build."

    doNotTrackState("State tracking disabled for Jacoco report task")

    val reportsDir = file("build/reports/jacoco/test")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${reportsDir}/jacocoTestReport.xml"))
        html.outputLocation.set(file("${reportsDir}/html"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "*/R$.class",
        "*/BuildConfig.",
        "*/Manifest.*",
        "*/*Test.*",
        "*/Hilt.*",
        "*/di/*"
    )

    // Directorios con clases compiladas (Java + Kotlin)
    val debugTree = fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    // Fuentes
    sourceDirectories.setFrom(files(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin"
    ))

    classDirectories.setFrom(files(debugTree))

    // Ruta para executionData
    executionData.setFrom(fileTree(layout.buildDirectory.get().asFile).include(
        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
    ))
}



dependencies {
    // Dependencias de AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.gson)
    implementation(libs.jsoup.v1153)
    implementation(libs.squareup.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.coil.compose)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.generativeai)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.com.amazonaws.aws.android.sdk.s3)
    implementation(libs.firebase.database)
    implementation(libs.androidx.appcompat)
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:11.1.0")

    // Dependencias de Room
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.ui.test.junit4.android)
    kapt("androidx.room:room-compiler:2.6.1")

    // Dependencias de pruebas
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation ("org.robolectric:robolectric:4.9")

    // Herramientas para pruebas de corutinas
    testImplementation(libs.kotlinx.coroutines.test)
}
