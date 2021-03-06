rootProject.name = "probation-integration-services"
include("libs:probation-integration-commons", "prison-case-notes-to-probation")

// load children from the "projects" directory (and drop the prefix)
rootProject.children
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("amazon-sqs", "com.amazonaws:amazon-sqs-java-messaging-lib:2.0.0")
            library("insights", "com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
            library("hawtio", "io.hawt:hawtio-springboot:2.15.0")
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.0.0")
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3")
            library("sentry", "io.sentry:sentry-spring-boot-starter:6.3.0")
            library("wiremock", "com.github.tomakehurst:wiremock-jre8:2.33.2")
        }
    }
}
