import com.gorylenko.GenerateGitPropertiesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.NoArgExtension
import org.springframework.boot.gradle.tasks.buildinfo.BuildInfo
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import uk.gov.justice.digital.hmpps.plugins.ClassPathPlugin
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22" apply false
    kotlin("plugin.jpa") version "1.9.22" apply false
    kotlin("kapt") version "1.9.22" apply false
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("com.gorylenko.gradle-git-properties") version "2.4.1" apply false
    id("com.google.cloud.tools.jib") apply false
    id("base")
    id("org.sonarqube")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.5.0")
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
            sourceCompatibility = "21"
        }

        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "21"
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
    apply { plugin("org.jetbrains.kotlin.kapt") }
    apply { plugin("org.jetbrains.kotlin.plugin.jpa") }
    apply { plugin("org.jetbrains.kotlin.plugin.spring") }
    apply { plugin("jacoco") }
    apply { plugin("test-report-aggregation") }
    apply { plugin("jacoco-report-aggregation") }
    apply { plugin("org.sonarqube") }
    apply { plugin("com.gorylenko.gradle-git-properties") }
    apply { plugin(JibConfigPlugin::class.java) }
    apply { plugin(ClassPathPlugin::class.java) }

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
