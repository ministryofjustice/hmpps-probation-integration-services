plugins {
    alias(build.plugins.jib) apply false
    alias(build.plugins.sonarqube) apply false
    `kotlin-dsl`
}

dependencies {
    implementation(build.jib)
    implementation(build.sonarqube)
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

tasks {
    jar { outputs.cacheIf { true } }
}
