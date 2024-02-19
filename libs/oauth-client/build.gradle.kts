dependencies {
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    api("org.springframework.boot:spring-boot-starter-oauth2-client")
}

configure<uk.gov.justice.digital.hmpps.extensions.ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**"
    )
}
