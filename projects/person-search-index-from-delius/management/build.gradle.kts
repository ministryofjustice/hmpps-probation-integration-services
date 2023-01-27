apply(plugin = "com.palantir.docker")

tasks {
    configure<com.palantir.gradle.docker.DockerExtension> {
        name = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/person-search-index-from-delius-management:${project.version}"
        tag("latest", "ghcr.io/ministryofjustice/hmpps-probation-integration-services/person-search-index-from-delius-management:latest")
        files("container")
    }
}
