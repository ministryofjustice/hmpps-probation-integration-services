rootProject.name = "probation-integration-services"
include(
    // ⌄ add new projects here
    "justice-email-and-delius",
    "appointment-reminders-and-delius",
    "ims-and-delius",
    "common-platform-and-delius",
    "subject-access-requests-and-delius",
    "accredited-programmes-and-oasys",
    "approved-premises-and-delius",
    "approved-premises-and-oasys",
    "arns-and-delius",
    "assessment-summary-and-delius",
    "cas2-and-delius",
    "cas3-and-delius",
    "core-person-record-and-delius",
    "court-case-and-delius",
    "create-and-vary-a-licence-and-delius",
    "custody-key-dates-and-delius",
    "domain-events-and-delius",
    "dps-and-delius",
    "effective-proposal-framework-and-delius",
    "external-api-and-delius",
    "feature-flags",
    "hdc-licences-and-delius",
    "hmpps-auth-and-delius",
    "make-recall-decisions-and-delius",
    "manage-offences-and-delius",
    "manage-pom-cases-and-delius",
    "manage-supervision-and-delius",
    "manage-supervision-and-oasys",
    "oasys-and-delius",
    "offender-events-and-delius",
    "opd-and-delius",
    "pathfinder-and-delius",
    "person-search-index-from-delius",
    "pre-sentence-reports-to-delius",
    "prison-case-notes-to-probation",
    "prison-custody-status-to-delius",
    "prison-education-and-delius",
    "prison-identifier-and-delius",
    "prisoner-profile-and-delius",
    "probation-search-and-delius",
    "redrive-dead-letter-queues",
    "refer-and-monitor-and-delius",
    "resettlement-passport-and-delius",
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
    "libs:document-management",
    "libs:messaging",
    "libs:oauth-client",
    "libs:oauth-server",
    "libs:limited-access",
    "libs:prison-staff"
)

// load children from the "projects" directory (and drop the prefix)
fun ProjectDescriptor.allChildren(): Set<ProjectDescriptor> = children + children.flatMap { it.allChildren() }
rootProject.allChildren()
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("asyncapi", "org.openfolder:kotlin-asyncapi-spring-web:3.0.3")
            library("aws-autoconfigure", "io.awspring.cloud:spring-cloud-aws-autoconfigure:3.2.1")
            library("aws-query-protocol", "software.amazon.awssdk:aws-query-protocol:2.29.9")
            library("aws-sns", "io.awspring.cloud:spring-cloud-aws-starter-sns:3.2.1")
            library("aws-sqs", "io.awspring.cloud:spring-cloud-aws-starter-sqs:3.2.1")
            library("aws-starter", "io.awspring.cloud:spring-cloud-aws-starter:3.2.1")
            library("aws-sts", "software.amazon.awssdk:sts:2.29.9")
            library("azure-app-insights", "com.microsoft.azure:applicationinsights-web:3.6.2")
            library("azure-identity", "com.azure:azure-identity:1.14.1")
            library("flipt", "io.flipt:flipt-java:1.1.1")
            library("html2md", "com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
            library("microsoft-graph", "com.microsoft.graph:microsoft-graph:6.20.0")
            library("mockito-inline", "org.mockito:mockito-inline:5.2.0")
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:5.4.0")
            library("notify", "uk.gov.service.notify:notifications-java-client:5.2.1-RELEASE")
            library(
                "opentelemetry-annotations",
                "io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.9.0"
            )
            library("sentry", "io.sentry:sentry-spring-boot-starter-jakarta:7.16.0")
            library("springdoc", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
            library("wiremock", "org.wiremock:wiremock-standalone:3.9.2")

            bundle(
                "aws-messaging",
                listOf("aws-autoconfigure", "aws-starter", "aws-sns", "aws-sqs", "aws-sts", "aws-query-protocol")
            )
            bundle("mockito", listOf("mockito-kotlin", "mockito-inline"))
            bundle("telemetry", listOf("azure-app-insights", "opentelemetry-annotations", "sentry"))
        }
    }
}

plugins { id("com.gradle.develocity") version "3.18.1" }
develocity {
    buildScan {
        publishing.onlyIf { !System.getenv("CI").isNullOrEmpty() }
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}
