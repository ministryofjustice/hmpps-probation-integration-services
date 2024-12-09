plugins {
    id("com.google.cloud.tools.jib") version "3.4.4" apply false
    id("org.sonarqube") version "6.0.1.5171" apply false
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.4")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:6.0.0.5145")
}
