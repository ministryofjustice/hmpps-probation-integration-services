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
    "person-search-index-from-delius:management",
    "risk-assessment-scores-to-delius",
    "tier-to-delius",
    "approved-premises-and-delius",
    "approved-premises-and-oasys",
    "offender-events-and-delius",
    "custody-key-dates-and-delius",
    "make-recall-decisions-and-delius",
    "unpaid-work-and-delius",
    "manage-pom-cases-and-delius",
    "refer-and-monitor-and-delius",
    // ^ add new projects here
)

// load children from the "projects" directory (and drop the prefix)
fun ProjectDescriptor.allChildren(): Set<ProjectDescriptor> = children + children.flatMap { it.allChildren() }
rootProject.allChildren()
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("aws-autoconfigure", "io.awspring.cloud:spring-cloud-aws-autoconfigure:3.0.0-RC1")
            library("aws-starter", "io.awspring.cloud:spring-cloud-aws-starter:3.0.0-RC1")
            library("aws-sns", "io.awspring.cloud:spring-cloud-aws-starter-sns:3.0.0-RC1")
            library("aws-sqs", "io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.0-RC1")
            bundle("aws-messaging", listOf("aws-autoconfigure", "aws-starter", "aws-sns", "aws-sqs"))
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.1.0")
            library("mockito-inline", "org.mockito:mockito-inline:5.1.1")
            bundle("mockito", listOf("mockito-kotlin", "mockito-inline"))
            library("insights", "com.microsoft.azure:applicationinsights-web:3.4.9")
            library("sentry", "io.sentry:sentry-spring-boot-starter-jakarta:6.14.0")
            library("opentelemetry-annotations", "io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:1.22.1")
            bundle("telemetry", listOf("insights", "opentelemetry-annotations", "sentry"))
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:4.0.1")
            library("springdoc", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
            library("wiremock", "com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
            library("mapstruct", "org.mapstruct:mapstruct:1.5.3.Final")
            library("mapstructprocessor", "org.mapstruct:mapstruct-processor:1.5.3.Final")
        }
    }
}

plugins { id("com.gradle.enterprise") version "3.12.3" }
gradleEnterprise {
    buildScan {
        publishAlwaysIf(!System.getenv("CI").isNullOrEmpty())
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
