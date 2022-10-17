rootProject.name = "probation-integration-services"
include(
    "libs:audit",
    "libs:commons",
    "libs:dev-tools",
    "libs:oauth-client",
    "libs:oauth-server",
    "prison-case-notes-to-probation",
    "workforce-allocations-to-delius",
    "pre-sentence-reports-to-delius",
    "prison-custody-status-to-delius",
    "person-search-index-from-delius",
    "risk-assessment-scores-to-delius",
    "tier-to-delius",
    // ^ add new projects here
)

// load children from the "projects" directory (and drop the prefix)
rootProject.children
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("amazon-sqs", "com.amazonaws:amazon-sqs-java-messaging-lib:2.0.1")
            library("insights", "com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
            library("hawtio", "io.hawt:hawtio-springboot:2.16.0")
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.0.0")
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:3.1.4")
            library("sentry", "io.sentry:sentry-spring-boot-starter:6.5.0")
            library("wiremock", "com.github.tomakehurst:wiremock-jre8:2.34.0")
            library("swagger-docs", "org.springdoc:springdoc-openapi-ui:1.6.11")
        }
    }
}
