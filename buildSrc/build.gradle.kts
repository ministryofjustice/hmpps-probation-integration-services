plugins {
    id("com.google.cloud.tools.jib") version "3.4.0" apply false
    id("org.sonarqube") version "4.2.1.3168" apply false
   `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.3.2")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.2.1.3168")
}