plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "3.3" apply true
}

val sonarToken = findProperty("sonar.token") as String?

sonarqube {
    properties {
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin,src/test/java")
        property("sonar.token", sonarToken ?: "")
        property("sonar.projectKey", "ivaanesteepar_FlixFinderTV")
        property("sonar.organization", "ivaanesteepar")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}
