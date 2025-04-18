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

        buildConfigField("String", "apiKey", "\"AIzaSyAHR1-WLxXl3sbcABH-vPyLJT4nnBfHcDk\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        // Importante para JaCoCo: habilitar cobertura en debug
        debug {
            enableUnitTestCoverage = true // cobertura para tests unitarios
            enableAndroidTestCoverage = true // (opcional) cobertura para tests instrumentados
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

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
        html.outputLocation.set(file("${buildDir}/reports/jacoco/jacocoTestReport/html"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/*$*$*.*",
        "**/di/**",
        "**/Hilt*.*",
        "**/*_MembersInjector.class",
        "**/Dagger*Component*.class"
    )

    // Directorios con clases compiladas (Java + Kotlin)
    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    // Fuentes
    sourceDirectories.setFrom(files(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin"
    ))

    classDirectories.setFrom(files(debugTree))

    // Ruta corregida para executionData
    executionData.setFrom(fileTree(buildDir).include(
        "jacoco/testDebugUnitTest.exec",
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
    testImplementation(libs.mockk)  // MockK para pruebas en Kotlin
    testImplementation ("androidx.arch.core:core-testing:2.1.0")

    // Herramientas para pruebas de corutinas
    testImplementation(libs.kotlinx.coroutines.test)  // Para pruebas de corutinas
}


