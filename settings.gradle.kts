rootProject.name = "probation-integration-services"
include("prison-case-notes-to-probation")

// load children from the "projects" directory (and drop the prefix)
rootProject.children.forEach { it.projectDir = File(rootDir, "projects/${it.projectDir.relativeTo(rootDir)}") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("hawtio", "io.hawt:hawtio-springboot:2.15.0")
            library("hoverfly", "io.specto:hoverfly-java:0.14.0")
            library("openFeign", "org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3")
        }
    }
}