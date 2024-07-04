plugins {
    id("com.google.cloud.tools.jib") version "3.4.3" apply false
    id("org.sonarqube") version "5.1.0.4882" apply false
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.3")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:5.1.0.4882")
}
