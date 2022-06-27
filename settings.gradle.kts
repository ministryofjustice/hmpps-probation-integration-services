rootProject.name = "probation-integration-services"
include("libs:probation-integration-commons", "prison-case-notes-to-probation")

// load children from the "projects" directory (and drop the prefix)
rootProject.children
    .filter { !it.path.startsWith(":libs") }
    .forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("amazon-sqs", "com.amazonaws:amazon-sqs-java-messaging-lib:1.1.0")
            library("hawtio", "io.hawt:hawtio-springboot:2.15.0")
            library("hoverfly", "io.specto:hoverfly-java:0.14.2")
            library("hoverfly-junit", "io.specto:hoverfly-java-junit5:0.14.2")
            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:4.0.0")
            library("openfeign", "org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3")
        }
    }
}
