rootProject.name = "probation-integration-services"
include(
    // ⌄ add new projects here
    "hmpps-auth-and-delius",
    "opd-and-delius",
    "prison-education-and-delius",
    "resettlement-passport-and-delius",
    "manage-offences-and-delius",
    "approved-premises-and-delius",
    "approved-premises-and-oasys",
    "court-case-and-delius",
    "create-and-vary-a-licence-and-delius",
    "custody-key-dates-and-delius",
    "domain-events-and-delius",
    "effective-proposal-framework-and-delius",
    "external-api-and-delius",
    "make-recall-decisions-and-delius",
    "manage-pom-cases-and-delius",
    "offender-events-and-delius",
    "pathfinder-and-delius",
    "person-search-index-from-delius",
    "pre-sentence-reports-to-delius",
    "prison-case-notes-to-probation",
    "prison-custody-status-to-delius",
    "refer-and-monitor-and-delius",
    "risk-assessment-scores-to-delius",
    "sentence-plan-and-delius",
    "sentence-plan-and-oasys",
    "soc-and-delius",
    "tier-to-delius",
    "unpaid-work-and-delius",
    "workforce-allocations-to-delius",
    "libs:audit",
    "libs:commons",
    "libs:dev-tools",
    "libs:messaging",
    "libs:oauth-client",
    "libs:oauth-server",
    "libs:limited-access"
)

// load children from the "projects" directory (and drop the prefix)
fun ProjectDescriptor.allChildren(): Set<ProjectDescriptor> = children + children.flatMap { it.allChildren() }
rootProject.allChildren()
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("aws-autoconfigure", "io.awspring.cloud:spring-cloud-aws-autoconfigure:3.0.2")
            library("aws-starter", "io.awspring.cloud:spring-cloud-aws-starter:3.0.2")
            library("aws-sns", "io.awspring.cloud:spring-cloud-aws-starter-sns:3.0.2")
            library("aws-sqs", "io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.2")
            library("aws-sts", "software.amazon.awssdk:sts:2.20.92")
            bundle("aws-messaging", listOf("aws-autoconfigure", "aws-starter", "aws-sns", "aws-sqs", "aws-sts"))
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:5.1.0")
            library("mockito-inline", "org.mockito:mockito-inline:5.2.0")
            bundle("mockito", listOf("mockito-kotlin", "mockito-inline"))
            library("insights", "com.microsoft.azure:applicationinsights-web:3.4.17")
            library("sentry", "io.sentry:sentry-spring-boot-starter-jakarta:6.31.0")
            library("opentelemetry-annotations", "io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:1.30.0")
            bundle("telemetry", listOf("insights", "opentelemetry-annotations", "sentry"))
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:4.0.4")
            library("springdoc", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
            library("wiremock", "com.github.tomakehurst:wiremock-jre8-standalone:3.0.1")
            library("mapstruct", "org.mapstruct:mapstruct:1.5.5.Final")
            library("mapstructprocessor", "org.mapstruct:mapstruct-processor:1.5.5.Final")
            library("flipt", "io.flipt:flipt-java:0.1.8")
        }
    }
}

plugins { id("com.gradle.enterprise") version "3.15.1" }
gradleEnterprise {
    buildScan {
        publishAlwaysIf(!System.getenv("CI").isNullOrEmpty())
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
