plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "3.3" apply true
}

sonarqube {
    properties {
        // Fuentes de producción
        property("sonar.sources", "src/main/kotlin")

        // Archivos de prueba (mejor separados por coma sin espacios)
        property("sonar.tests", "src/test/kotlin,src/test/java")

        // ⚠️ TOKEN DE AUTENTICACIÓN - solo si es un entorno local seguro (¡no subas este valor a GitHub!)
        property("sonar.token", "18450ef60b74b77383c26813c611b29606b8ef3f")

        // ⚠️ Claves necesarias para SonarCloud
        property("sonar.projectKey", "ivaanesteepar_FlixFinderTV")
        property("sonar.organization", "ivaanesteepar")
        property("sonar.host.url", "https://sonarcloud.io")

        // Informe de cobertura
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

    }
}





