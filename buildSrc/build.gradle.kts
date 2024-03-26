plugins {
    id("com.google.cloud.tools.jib") version "3.4.1" apply false
    id("org.sonarqube") version "5.0.0.4638" apply false
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.1")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.4.1.3373")
}