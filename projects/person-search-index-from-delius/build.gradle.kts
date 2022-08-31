apply(plugin = "com.palantir.docker")

tasks {
    configure<com.palantir.gradle.docker.DockerExtension> {
        name = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}:${project.version}"
        tag("latest", "ghcr.io/ministryofjustice/hmpps-probation-integration-services/${project.name}:latest")
        files(file("container").listFiles())
    }
}
