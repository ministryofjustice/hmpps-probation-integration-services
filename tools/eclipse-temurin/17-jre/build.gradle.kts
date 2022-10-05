apply(plugin = "com.palantir.docker")

tasks {
    configure<com.palantir.gradle.docker.DockerExtension> {
        name = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/eclipse-temurin:17-jre-alpine"
        tag("17-jre-alpine", "ghcr.io/ministryofjustice/hmpps-probation-integration-services/eclipse-temurin:17-jre-alpine")
    }
}
