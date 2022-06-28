import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import uk.gov.justice.digital.hmpps.plugins.JibConfigPlugin

plugins {
    kotlin("jvm") version "1.7.0" apply false
    kotlin("plugin.spring") version "1.7.0" apply false
    kotlin("plugin.jpa") version "1.7.0" apply false
    id("org.springframework.boot") version "2.7.1" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    id("com.google.cloud.tools.jib") apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("base")
    id("jacoco")
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
}

val agentDeps: Configuration by configurations.creating

dependencies {
    agentDeps("com.microsoft.azure:applicationinsights-agent:3.3.0")
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

val exclusions = listOf(
    "**/ThreadConfig*",
    "**/ContextRunnable*",
    "**/ConnectionProviderConfig*",
    "**/entity/**",
    "**/AppKt.class"
)

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
    }

    val dev: Configuration by configurations.creating {
        extendsFrom(configurations["implementation"])
        extendsFrom(configurations["runtimeOnly"])
    }

    configure<SourceSetContainer> {
        val main by getting

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

        tasks {
            withType<org.springframework.boot.gradle.tasks.run.BootRun> {
                if (System.getProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE")) == "dev") {
                    classpath = dev.runtimeClasspath
                }
            }

            named<JacocoReport>("jacocoTestReport") {
                classDirectories.setFrom(
                    files(
                        classDirectories.files.map { fileTree(it) { exclude(exclusions) } }
                    )
                )
                executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
            }
            create<Test>("integrationTest") {
                testClassesDirs = integrationTest.output.classesDirs
                classpath = integrationTest.runtimeClasspath
            }
            withType<Test> {
                useJUnitPlatform()
                finalizedBy("jacocoTestReport")
            }
            named("check") {
                dependsOn("ktlintCheck", "test", "integrationTest")
            }
        }
    }
}

project.ext.set("Stevo", "A special message for stevo")
project.ext.set("functionForStevo", {println("Running this for Stevo")})

// Aggregate jacoco report across sub-projects
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(subprojects.map { it.tasks.getByName("jacocoTestReport") })
    val main = subprojects.map { it.sourceSets.named("main").get() }
    additionalSourceDirs(files(main.map { it.allSource.srcDirs }))
    additionalClassDirs(
        files(
            main.map { it.output }.map { it.flatMap { file -> fileTree(file) { exclude(exclusions) } } }
        )
    )
    executionData(files(subprojects.map { it.tasks.named<JacocoReport>("jacocoTestReport").get().executionData }))
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}
tasks.named("check") { dependsOn("ktlintCheck", "jacocoTestReport") }
