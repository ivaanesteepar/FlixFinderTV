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
        property("sonar.sources", "app/src/main/kotlin")

        // Solo archivos de prueba
        property("sonar.tests", "app/src/test/kotlin, app/src/test/java")

        // Excluir el directorio de producción de los tests
        property("sonar.test.exclusions", "app/src/main/kotlin/com/example/flixfindertv/**")  // Excluir todo el código de producción

        // Ruta del informe de cobertura JaCoCo
        property("sonar.jacoco.reportPaths", "${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}




