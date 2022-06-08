import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.0" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21" apply false
    id("com.google.cloud.tools.jib") version "3.2.1" apply false
}

val agentDeps: Configuration by configurations.creating

project.dependencies.add("agentDeps", "com.microsoft.azure:applicationinsights-agent:3.2.11")
val copyAgentTask = project.tasks.register("copyAgent", Copy::class.java) {
    from(agentDeps)
    into("${project.buildDir}/agent")
    rename("applicationinsights-agent(.+).jar", "agent.jar")
}

tasks.getByName("assemble").dependsOn(copyAgentTask)

allprojects {
    group = "uk.gov.justice.digital"

    repositories {
        mavenLocal()
        mavenCentral()
    }

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

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("com.google.cloud.tools.jib")
    }
}
