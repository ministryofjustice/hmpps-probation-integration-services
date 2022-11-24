rootProject.name = "probation-integration-services"
include(
    "libs:audit",
    "libs:commons",
    "libs:messaging",
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
    "approved-premises-and-delius",
    "approved-premises-and-oasys",
    "domain-events-and-delius",
    "offender-events-and-delius",
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
            library("aws-messaging", "io.awspring.cloud:spring-cloud-aws-messaging:2.4.2")
            library("insights", "com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
            library("hawtio", "io.hawt:hawtio-springboot:2.16.1")
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.0.0")
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:3.1.5")
            library("sentry", "io.sentry:sentry-spring-boot-starter:6.8.0")
            library("springdoc-openapi-ui", "org.springdoc:springdoc-openapi-ui:1.6.13")
            library("springdoc-openapi-kotlin", "org.springdoc:springdoc-openapi-kotlin:1.6.12")
            bundle("swagger-docs", listOf("springdoc-openapi-ui", "springdoc-openapi-kotlin"))
            library("wiremock", "com.github.tomakehurst:wiremock-jre8:2.35.0")
        }
    }
}

plugins { id("com.gradle.enterprise") version "3.11.4" }
gradleEnterprise {
    buildScan {
        publishAlwaysIf(!System.getenv("CI").isNullOrEmpty())
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
