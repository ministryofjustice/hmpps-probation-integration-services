plugins {
    id("com.google.cloud.tools.jib") version "3.3.0" apply false
    id("org.sonarqube") version "3.5.0.2730" apply false
   `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.3.0")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.4.0.2513")
}