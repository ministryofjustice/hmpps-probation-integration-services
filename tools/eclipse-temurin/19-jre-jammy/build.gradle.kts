apply(plugin = "com.palantir.docker")

tasks {
    configure<com.palantir.gradle.docker.DockerExtension> {
        name = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/eclipse-temurin:19-jre-jammy"
        tag("19-jre-jammy", "ghcr.io/ministryofjustice/hmpps-probation-integration-services/eclipse-temurin:19-jre-jammy")
        buildx(true)
        push(System.getenv("PUSH") == "true")
        platform("linux/arm64", "linux/amd64")
    }
}
