dependencies {
    implementation(project(":libs:commons"))
    implementation(project(":libs:oauth-client"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.mockito)
}

configure<uk.gov.justice.digital.hmpps.extensions.ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**",
    )
}
