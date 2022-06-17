import com.google.cloud.tools.jib.gradle.JibExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.0" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.7.0" apply false
    kotlin("plugin.spring") version "1.7.0" apply false
    kotlin("plugin.jpa") version "1.7.0" apply false
    id("com.google.cloud.tools.jib") version "3.2.1" apply false
    id("base")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.2.11")
}

val copyAgentTask = project.tasks.register("copyAgent", Copy::class.java) {
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

        withType<Test> {
            useJUnitPlatform()
        }

        withType<BootJar> {
            enabled = false
        }

        withType<Jar> {
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
        plugin("com.google.cloud.tools.jib")
    }

    tasks {
        getByName("jib").dependsOn(copyAgentTask)
        getByName("jibBuildTar").dependsOn(copyAgentTask)
        getByName("jibDockerBuild").dependsOn(copyAgentTask)
    }

    configure<JibExtension> {
        container {
            creationTime = "USE_CURRENT_TIMESTAMP"
            jvmFlags = mutableListOf("-Duser.timezone=Europe/London")
            mainClass = "uk.gov.justice.digital.hmpps.AppKt"
            user = "2000:2000"
        }
        from {
            image = "eclipse-temurin:17-jre-alpine"
        }
        extraDirectories {
            paths {
                path {
                    setFrom("${rootProject.buildDir}")
                    includes.add("agent/agent.jar")
                }
                path {
                    setFrom("${project.projectDir}")
                    includes.add("applicationinsights*.json")
                    into = "/agent"
                }
            }
        }
    }

    val dev: Configuration by configurations.creating {
        extendsFrom(configurations["implementation"])
        extendsFrom(configurations["runtimeOnly"])
    }

    configure<SourceSetContainer> {
        val main by getting {
            if (System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE")) == "dev") {
                compileClasspath += configurations["dev"]
                runtimeClasspath += configurations["dev"]
            }
        }
        val dev by creating {
            compileClasspath += configurations["dev"] + main.compileClasspath + main.output
            runtimeClasspath += configurations["dev"] + main.runtimeClasspath + main.output
        }

        val test by getting {
            compileClasspath += configurations["dev"] + dev.output
            runtimeClasspath += configurations["dev"] + dev.output
        }

        val integrationTest by creating {
            compileClasspath += test.compileClasspath + test.output
            runtimeClasspath += test.runtimeClasspath + test.output
        }
    }
}
