rootProject.name = "probation-integration-services"
include(
    // ⌄ add new projects here
    "court-case-and-delius",
    "create-and-vary-a-licence-and-delius",
    "refer-and-monitor-and-delius",
    "manage-pom-cases-and-delius",
    "unpaid-work-and-delius",
    "make-recall-decisions-and-delius",
    "custody-key-dates-and-delius",
    "offender-events-and-delius",
    "approved-premises-and-oasys",
    "approved-premises-and-delius",
    "tier-to-delius",
    "risk-assessment-scores-to-delius",
    "person-search-index-from-delius",
    "prison-custody-status-to-delius",
    "pre-sentence-reports-to-delius",
    "workforce-allocations-to-delius",
    "prison-case-notes-to-probation",
    "libs:oauth-server",
    "libs:oauth-client",
    "libs:dev-tools",
    "libs:messaging",
    "libs:commons",
    "libs:audit"
)

// load children from the "projects" directory (and drop the prefix)
fun ProjectDescriptor.allChildren(): Set<ProjectDescriptor> = children + children.flatMap { it.allChildren() }
rootProject.allChildren()
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("aws-autoconfigure", "io.awspring.cloud:spring-cloud-aws-autoconfigure:3.0.0-RC2")
            library("aws-starter", "io.awspring.cloud:spring-cloud-aws-starter:3.0.0-RC2")
            library("aws-sns", "io.awspring.cloud:spring-cloud-aws-starter-sns:3.0.0-RC2")
            library("aws-sqs", "io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.0-RC2")
            bundle("aws-messaging", listOf("aws-autoconfigure", "aws-starter", "aws-sns", "aws-sqs"))
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.1.0")
            library("mockito-inline", "org.mockito:mockito-inline:5.2.0")
            bundle("mockito", listOf("mockito-kotlin", "mockito-inline"))
            library("insights", "com.microsoft.azure:applicationinsights-web:3.4.10")
            library("sentry", "io.sentry:sentry-spring-boot-starter-jakarta:6.17.0")
            library("opentelemetry-annotations", "io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:1.24.0")
            bundle("telemetry", listOf("insights", "opentelemetry-annotations", "sentry"))
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:4.0.2")
            library("springdoc", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
            library("wiremock", "com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
            library("mapstruct", "org.mapstruct:mapstruct:1.5.3.Final")
            library("mapstructprocessor", "org.mapstruct:mapstruct-processor:1.5.3.Final")
            library("flipt", "io.flipt:flipt-java:0.1.8")
        }
    }
}

plugins { id("com.gradle.enterprise") version "3.12.6" }
gradleEnterprise {
    buildScan {
        publishAlwaysIf(!System.getenv("CI").isNullOrEmpty())
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
