apply(plugin = "com.palantir.docker")

tasks {
    configure<com.palantir.gradle.docker.DockerExtension> {
        name = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}:${project.version}"
        tag("latest", "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}:latest")
        files("container")
    }
    named("docker") {
        doLast {
            val dir = File("${project.rootDir}/changed")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, project.name)
            if (!file.exists()) file.createNewFile()
        }
    }
}
