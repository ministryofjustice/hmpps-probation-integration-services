
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import uk.gov.justice.digital.hmpps.plugins.ClassPathPlugin
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "1.7.10" apply false
    kotlin("plugin.spring") version "1.7.10" apply false
    kotlin("plugin.jpa") version "1.7.10" apply false
    id("org.springframework.boot") version "2.7.2" apply false
    id("io.spring.dependency-management") version "1.0.12.RELEASE" apply false
    id("com.google.cloud.tools.jib") apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("base")
    id("jacoco")
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
    id("org.sonarqube")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.3.1")
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

        withType<BootJar> {
            enabled = false
        }

        if (!project.path.startsWith(":libs")) {
            withType<Jar> {
                enabled = false
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

// Aggregate jacoco report across sub-projects
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(subprojects.map { it.tasks.getByName("jacocoTestReport") })
    val main = subprojects.map { it.sourceSets.named("main").get() }
    additionalSourceDirs(files(main.map { it.allSource.srcDirs }))
    additionalClassDirs(
        files(
            subprojects.map {
                it.tasks.named<JacocoReport>("jacocoTestReport").get().classDirectories
            }
        )
    )
    executionData(files(subprojects.map { it.tasks.named<JacocoReport>("jacocoTestReport").get().executionData }))
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}
tasks.named("check") { dependsOn("ktlintCheck", "jacocoTestReport") }
