plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "3.3" apply true
}

sonarqube {
    properties {
        property("sonar.projectKey", "ivaanesteepar_FlixFinderTV")
        property("sonar.organization", "ivaanesteepar")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

        // RUTA CORRECTA A TUS TESTS
        property("sonar.tests", "app/src/test/java")

        // Asegura que reconozca bien el código fuente también
        property("sonar.sources", "app/src/main/java")
    }
}



