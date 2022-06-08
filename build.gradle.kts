import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.0" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.6.21" apply false
    kotlin("plugin.spring") version "1.6.21" apply false
    id("com.google.cloud.tools.jib") version "3.2.1" apply false
}

allprojects {
    group = "uk.gov.justice.digital"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<BootJar> {
        enabled = false
    }
}

// Add project name here to prevent attempting to create jib containers
val noContainerModules = listOf<String>()

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")

        if(project.name notIn noContainerModules) {
            plugin("com.google.cloud.tools.jib")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

infix fun String.notIn(list: List<String>): Boolean = !list.contains(this)
