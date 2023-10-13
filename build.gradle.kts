import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.NoArgExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import uk.gov.justice.digital.hmpps.plugins.ClassPathPlugin
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10" apply false
    kotlin("plugin.jpa") version "1.9.10" apply false
    kotlin("kapt") version "1.9.10" apply false
    id("org.springframework.boot") version "3.1.4" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
    id("com.google.cloud.tools.jib") apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("base")
    id("org.sonarqube")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.4.17")
}

val copyAgentTask = project.tasks.register<Copy>("copyAgent") {
    from(agentDeps)
    into("${project.layout.buildDirectory.dir("agent").get().asFile}")
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
    }
}

subprojects {
    apply { plugin("org.springframework.boot") }
    apply { plugin("io.spring.dependency-management") }
    apply { plugin("org.jetbrains.kotlin.jvm") }
    apply { plugin("org.jetbrains.kotlin.plugin.spring") }
    apply { plugin("org.jetbrains.kotlin.plugin.jpa") }
    apply { plugin("org.jlleitschuh.gradle.ktlint") }
    apply { plugin("jacoco") }
    apply { plugin("test-report-aggregation") }
    apply { plugin("jacoco-report-aggregation") }
    apply { plugin(JibConfigPlugin::class.java) }
    apply { plugin(ClassPathPlugin::class.java) }
    apply { plugin("org.sonarqube") }
    apply { plugin("org.jetbrains.kotlin.kapt") }

    tasks {
        withType<BootRun> {
            if (System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE"))?.split(",")?.contains("dev") == true) {
                classpath = sourceSets.getByName("dev").runtimeClasspath
            }
        }
        withType<Jar> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            archiveFileName.set("${archiveBaseName.get()}-${archiveClassifier.get()}.${archiveExtension.get()}")
        }
    }

    extensions.configure<NoArgExtension> {
        annotation("org.springframework.ldap.odm.annotations.Entry")
    }

    if (!path.startsWith(":libs")) {
        tasks.named("check") {
            dependsOn(rootProject.subprojects.filter { it.path.startsWith(":libs") }.map { it.tasks.getByName("check") })
        }
    }
}

tasks.named("check") { dependsOn("ktlintCheck") }
