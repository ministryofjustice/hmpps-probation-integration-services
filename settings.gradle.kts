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
            library("aws-autoconfigure", "io.awspring.cloud:spring-cloud-aws-autoconfigure:3.0.0-M3")
            library("aws-starter", "io.awspring.cloud:spring-cloud-aws-starter:3.0.0-M3")
            library("aws-sns", "io.awspring.cloud:spring-cloud-aws-starter-sns:3.0.0-M3")
            library("aws-sqs", "io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.0-M3")
            bundle("aws-messaging", listOf("aws-autoconfigure", "aws-starter", "aws-sns", "aws-sqs"))
            library("insights", "com.microsoft.azure:applicationinsights-web:3.4.5")
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.1.0")
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:4.0.0-RC3")
            library("sentry", "io.sentry:sentry-spring-boot-starter:6.9.1")
            library("springdoc", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.0")
            library("wiremock", "com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
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
