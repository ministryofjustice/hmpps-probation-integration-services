plugins {
    id("com.google.cloud.tools.jib") version "3.3.2" apply false
    id("org.sonarqube") version "4.0.0.2929" apply false
   `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.3.2")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.2.0.3127")
}