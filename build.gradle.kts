import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "3.3" apply true
}
val sonarToken: String? by lazy {
    System.getenv("SONAR_TOKEN")
        ?: run {
            val props = Properties()
            val localPropsFile = File(rootDir, "local.properties")
            if (localPropsFile.exists()) {
                props.load(localPropsFile.inputStream())
                props.getProperty("sonar.token")
            } else null
        }
}
println("sonar token: ${sonarToken?.take(4)}****")

sonarqube {
    properties {
        property("sonar.sources", "src/main/kotlin")
        property("sonar.token", sonarToken ?: "")
        property("sonar.projectKey", "ivaanesteepar_FlixFinderTV")
        property("sonar.organization", "ivaanesteepar")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}
