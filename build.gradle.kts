import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import uk.gov.justice.digital.hmpps.plugins.ClassPathPlugin
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0" apply false
    kotlin("plugin.jpa") version "1.8.0" apply false
    id("org.springframework.boot") version "3.0.1" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("com.google.cloud.tools.jib") apply false
    id("com.palantir.docker") version "0.34.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("base")
    id("org.sonarqube")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.4.7")
}

val copyAgentTask = project.tasks.register<Copy>("copyAgent") {
    from(agentDeps)
    into("${project.buildDir}/agent")
    rename("applicationinsights-agent(.+).jar", "agent.jar")
}

allprojects {
    group = "uk.gov.justice.digital"

    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
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

        withType<BootJar> {
            enabled = false
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

    tasks {
        withType<BootRun> {
            if (System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE")) == "dev") {
                classpath = sourceSets.getByName("dev").runtimeClasspath
            }
        }

        withType<Test> {
            if (!project.path.startsWith(":libs")) {
                systemProperty(
                    "java.util.logging.manager",
                    System.getProperty("java.util.logging.manager")
                )
            }
        }
    }
}
tasks.named("check") { dependsOn("ktlintCheck") }
