dependencies {
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    api("org.eclipse.jetty:jetty-client:12.1.3")
}

configure<uk.gov.justice.digital.hmpps.extensions.ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**"
    )
}
