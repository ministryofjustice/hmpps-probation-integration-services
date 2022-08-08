package uk.gov.justice.digital.hmpps.plugins

import com.google.cloud.tools.jib.gradle.JibExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.configure

class JibConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.google.cloud.tools.jib") {
            project.extensions.configure<JibExtension> {
                container {
                    creationTime = "USE_CURRENT_TIMESTAMP"
                    jvmFlags = mutableListOf("-Duser.timezone=Europe/London")
                    mainClass = "uk.gov.justice.digital.hmpps.AppKt"
                    user = "2000:2000"
                }
                from {
                    image = "eclipse-temurin:17-jre-alpine"
                }
                to {
                    image = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}:${project.version}"
                    auth {
                        username = System.getenv("GITHUB_USERNAME")
                        password = System.getenv("GITHUB_PASSWORD")
                    }
                    tags = mutableSetOf("latest")
                }
                extraDirectories {
                    paths {
                        path {
                            setFrom("${project.rootProject.buildDir}")
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

            val copyAgentTask = project.rootProject.tasks.named("copyAgent")
            project.tasks.named("jib") {
                dependsOn(copyAgentTask)
            }
            project.tasks.named("jibBuildTar") {
                dependsOn(copyAgentTask)
            }
            project.tasks.named("jibDockerBuild") {
                dependsOn(copyAgentTask)
            }
        }
    }
}