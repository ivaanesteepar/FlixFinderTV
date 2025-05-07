plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "3.3" apply true
}

sonarqube {
    properties {
        // Código fuente
        property("sonar.sources", "src/main/kotlin")

        // Tests (separados por coma, sin espacios)
        property("sonar.tests", "src/test/kotlin,src/test/java")

        // Claves de SonarCloud
        property("sonar.projectKey", "ivaanesteepar_FlixFinderTV")
        property("sonar.organization", "ivaanesteepar")
        property("sonar.host.url", "https://sonarcloud.io")

        // Ruta al reporte de cobertura generado por JaCoCo
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}






