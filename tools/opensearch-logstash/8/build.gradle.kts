apply(plugin = "com.palantir.docker")

tasks {
    configure<com.palantir.gradle.docker.DockerExtension> {
        name = "ghcr.io/ministryofjustice/hmpps-probation-integration-services/opensearch-logstash:8"
        tag("8", "ghcr.io/ministryofjustice/hmpps-probation-integration-services/opensearch-logstash:8")
    }
}