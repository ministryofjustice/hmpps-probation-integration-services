plugins {
    id("com.google.cloud.tools.jib") version "3.2.1" apply false
   `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:3.2.1")
}