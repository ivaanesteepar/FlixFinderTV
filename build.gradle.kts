plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "3.3" apply true
}

sonarqube {
    properties {
        property("sonar.projectKey", "com.example.flixfindertv")  // Ajusta esto a tu propio proyecto
        property("sonar.projectName", "FlixFinderTV")
        property("sonar.projectVersion", "1.0")
        property("sonar.token", "squ_f47a24e3ac663dec6795d954d40816376a468b66")
        property("sonar.sources", listOf("src/main/java"))  // O la ruta correspondiente a tu código
        property("sonar.tests", "src/androidTest/java")  // Rutas de tests
        property("sonar.java.binaries", "build/intermediates/classes/debug")  // Compilados del código
//        property("sonar.androidLint.reportPaths", "build/reports/lint-results.xml")  // Informe de lint
//        property("sonar.junit.reportPaths", "build/test-results/testDebugUnitTest/")  // Resultados de JUnit
//        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/testDebugUnitTestCoverage/testDebugUnitTestCoverage.xml")  // Cobertura de tests
    }
}


