package uk.gov.justice.digital.hmpps.plugins

import com.google.cloud.tools.jib.gradle.BuildImageTask
import com.google.cloud.tools.jib.gradle.JibExtension
import com.google.cloud.tools.jib.gradle.JibTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

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
                    image = "docker://ghcr.io/ministryofjustice/hmpps-probation-integration-services/eclipse-temurin:17-jre-jammy@${System.getenv("JRE_17_JAMMY_SHA")}"
                }
                to {
                    image = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}"
                    auth {
                        username = System.getenv("GITHUB_USERNAME")
                        password = System.getenv("GITHUB_PASSWORD")
                    }
                }
                extraDirectories {
                    paths {
                        path {
                            setFrom("${project.rootProject.buildDir}")
                            includes.add("agent/agent.jar")
                        }
                        path {
                            setFrom("${project.buildDir}/agent")
                            includes.add("applicationinsights*.json")
                            into = "/agent"
                        }
                    }
                }
            }

            val copyAgent = project.rootProject.tasks.named("copyAgent")
            val copyAppInsightsConfig = project.tasks.register<Copy>("copyAppInsightsConfig") {
                from("${project.projectDir}/applicationinsights.json")
                into("${project.buildDir}/agent")
                inputs.file("${project.projectDir}/applicationinsights.json")
                outputs.file("${project.buildDir}/agent/applicationinsights.json")
                outputs.cacheIf { true }
            }
            val assemble = project.tasks.named("assemble")
            project.tasks.withType<BuildImageTask>().named("jib") {
                doFirst {
                    jib!!.to.tags = setOf("${project.version}")
                }
                dependsOn(copyAgent, `copyAppInsightsConfig`, assemble)
                inputs.dir("deploy")
                inputs.dir("src")
                inputs.file("build.gradle.kts")
                outputs.file("${project.buildDir}/jib-image.id")
                outputs.cacheIf { true }
            }
        }
    }
}