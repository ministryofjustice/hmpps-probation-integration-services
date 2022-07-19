package uk.gov.justice.digital.hmpps.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.tasks.JacocoReport
import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

class ClassPathPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("classPath", ClassPathExtension::class.java)

        project.configurations.create("dev") {
            extendsFrom(project.configurations["implementation"])
            extendsFrom(project.configurations["runtimeOnly"])
        }

        project.configure<SourceSetContainer> {
            val main: SourceSet = getByName("main")

            create("dev") {
                compileClasspath += project.configurations["dev"] + main.compileClasspath + main.output
                runtimeClasspath += project.configurations["dev"] + main.runtimeClasspath + main.output
            }

            getByName("test") {
                compileClasspath += project.configurations["dev"] + getByName("dev").output
                runtimeClasspath += project.configurations["dev"] + getByName("dev").output
            }

            create("integrationTest") {
                compileClasspath += getByName("test").compileClasspath + getByName("test").output
                runtimeClasspath += getByName("test").runtimeClasspath + getByName("test").output
            }

            project.tasks.named<JacocoReport>("jacocoTestReport").configure {
                classDirectories.setFrom(
                    project.files(
                        classDirectories.files.map { project.fileTree(it) { exclude(extension.jacocoExclusions) } }
                    )
                )
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                }
                executionData.setFrom(project.fileTree(project.buildDir).include("/jacoco/*.exec"))
            }

            project.tasks.create("integrationTest", Test::class.java) {
                testClassesDirs = getByName("integrationTest").output.classesDirs
                classpath = getByName("integrationTest").runtimeClasspath
            }
            project.tasks.withType(Test::class.java) {
                useJUnitPlatform()
                finalizedBy("jacocoTestReport")
            }
            project.tasks.named("check") {
                dependsOn("ktlintCheck", "test", "integrationTest")
            }
        }
    }
}
