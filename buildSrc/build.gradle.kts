plugins {
    id("com.google.cloud.tools.jib") version "3.2.1" apply false
    id("org.sonarqube") version "3.4.0.2513" apply false
   `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:3.2.1")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.4.0.2513")
}