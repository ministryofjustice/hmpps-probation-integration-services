
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun
import uk.gov.justice.digital.hmpps.plugins.ClassPathPlugin
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21" apply false
    kotlin("plugin.jpa") version "1.7.22" apply false
    id("org.springframework.boot") version "2.7.6" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("com.google.cloud.tools.jib") apply false
    id("com.palantir.docker") version "0.34.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("base")
    id("org.sonarqube")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.4.4")
}

val copyAgentTask = project.tasks.register<Copy>("copyAgent") {
    from(agentDeps)
    into("${project.buildDir}/agent")
    rename("applicationinsights-agent(.+).jar", "agent.jar")
}

allprojects {
    group = "uk.gov.justice.digital"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "17"
        }

        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "17"
            }
        }
    }
}

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("jacoco")
        plugin("test-report-aggregation")
        plugin("jacoco-report-aggregation")
        plugin(JibConfigPlugin::class.java)
        plugin(ClassPathPlugin::class.java)
        plugin("org.sonarqube")
    }

    tasks.withType<BootRun> {
        if (System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE")) == "dev") {
            classpath = sourceSets.getByName("dev").runtimeClasspath
        }
    }
}
tasks.named("check") { dependsOn("ktlintCheck") }
