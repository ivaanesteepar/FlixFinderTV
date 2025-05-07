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

        val apiKey: String = project.findProperty("API_KEY") as String? ?: ""

        android {
            defaultConfig {
                buildConfigField("String", "API_KEY", "\"$apiKey\"")
            }
        }

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
    dependsOn("testDebugUnitTest") // Verifica que este sea el nombre correcto de la tarea de pruebas
    reports {
        xml.required.set(true) // Generar reporte en XML
        html.required.set(true) // Generar reporte en HTML
    }

    // Asegúrate de que la ruta a las clases esté correcta
    classDirectories.setFrom(
        fileTree("build/tmp/kotlin-classes/debug") {
            exclude(
                "**/R.class",
                "*/R$.class",
                "*/BuildConfig.",
                "*/Manifest.*",
                "*/*Test.*"
            )
        }
    )

    // Asegúrate de que las fuentes estén configuradas correctamente
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir).include("jacoco/testDebugUnitTest.exec"))
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
    testImplementation ("org.robolectric:robolectric:4.9")

    // Herramientas para pruebas de corutinas
    testImplementation(libs.kotlinx.coroutines.test)  // Para pruebas de corutinas
}