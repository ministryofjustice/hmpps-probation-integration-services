import com.gorylenko.GenerateGitPropertiesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.noarg.gradle.NoArgExtension
import org.springframework.boot.gradle.tasks.buildinfo.BuildInfo
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import uk.gov.justice.digital.hmpps.plugins.ClassPathPlugin
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0" apply false
    kotlin("plugin.jpa") version "2.0.0" apply false
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("com.gorylenko.gradle-git-properties") version "2.4.2" apply false
    id("com.google.cloud.tools.jib") apply false
    id("base")
    id("org.sonarqube")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.5.2")
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

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict") // to make use of Spring's null-safety annotations
        }
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "21"
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
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("jacoco")
        plugin("test-report-aggregation")
        plugin("jacoco-report-aggregation")
        plugin("org.sonarqube")
        plugin("com.gorylenko.gradle-git-properties")
        plugin(JibConfigPlugin::class.java)
        plugin(ClassPathPlugin::class.java)
    }

    tasks {
        withType<BootRun> {
            val profiles = System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE"))
            if (profiles?.split(",")?.contains("dev") == true) {
                classpath = sourceSets.getByName("dev").runtimeClasspath
            }
        }
        withType<Jar> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            archiveFileName.set("${archiveBaseName.get()}-${archiveClassifier.get()}.${archiveExtension.get()}")
        }
        named<GenerateGitPropertiesTask>("generateGitProperties") { enabled = false }
        register<GenerateGitPropertiesTask>("gitInfo") {
            gitProperties.gitPropertiesResourceDir = projectDir
        }
        register<BuildInfo>("buildInfo") {
            destinationDir = projectDir
        }
    }

    extensions.configure<NoArgExtension> {
        annotation("org.springframework.ldap.odm.annotations.Entry")
    }

    if (!path.startsWith(":libs")) {
        tasks.named("check") {
            dependsOn(rootProject.subprojects.filter { it.path.startsWith(":libs") }
                .map { it.tasks.getByName("check") })
        }
    }
}
