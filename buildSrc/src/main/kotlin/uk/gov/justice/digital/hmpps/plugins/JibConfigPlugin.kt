package uk.gov.justice.digital.hmpps.plugins

import com.google.cloud.tools.jib.gradle.BuildImageTask
import com.google.cloud.tools.jib.gradle.JibExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

class JibConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.google.cloud.tools.jib") {
            project.extensions.configure<JibExtension> {
                container {
                    jvmFlags = mutableListOf("-Duser.timezone=Europe/London")
                    mainClass = "uk.gov.justice.digital.hmpps.AppKt"
                    user = "2000:2000"
                }
                from {
                    image = System.getenv("JIB_FROM_IMAGE") ?: "eclipse-temurin:17-jre-alpine"
                }
                to {
                    image = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}"
                }
                extraDirectories {
                    paths {
                        path {
                            setFrom("${project.rootProject.layout.buildDirectory.get().asFile}")
                            includes.add("agent/agent.jar")
                        }
                        path {
                            setFrom("${project.layout.buildDirectory.dir("agent").get().asFile}")
                            includes.add("applicationinsights*.json")
                            into = "/agent"
                        }
                    }
                }
            }

            val copyAgent = project.rootProject.tasks.named("copyAgent")
            val copyAppInsightsConfig = project.tasks.register<Copy>("copyAppInsightsConfig") {
                from("${project.projectDir}/applicationinsights.json")
                into("${project.layout.buildDirectory.dir("agent").get().asFile}")
            }
            val assemble = project.tasks.named("assemble")
            project.tasks.withType<BuildImageTask>().named("jib") {
                doFirst {
                    jib!!.to {
                        tags = setOf("${project.version}")
                        auth {
                            username = System.getenv("GITHUB_USERNAME")
                            password = System.getenv("GITHUB_PASSWORD")
                        }
                    }
                }
                if (System.getenv("FORCE_DEPLOY") == "true") {
                    jib!!.to.tags = setOf("${project.version}")
                }
                doLast {
                    val dir = File("${project.rootDir}/changed")
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, project.name)
                    if (!file.exists()) file.createNewFile()
                }
                dependsOn(copyAgent, copyAppInsightsConfig, assemble)
                inputs.dir("deploy")
                val buildDir = project.layout.buildDirectory.get().asFile.path
                inputs.files(
                    "$buildDir/agent",
                    "$buildDir/classes",
                    "$buildDir/generated",
                    "$buildDir/resources",
                    project.configurations[jib!!.configurationName.get()].resolvedConfiguration.files
                )
                outputs.file("$buildDir/jib-image.id")
                outputs.cacheIf { true }
            }
        }
    }
}