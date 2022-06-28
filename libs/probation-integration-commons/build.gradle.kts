import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.insights)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/ThreadConfig*",
        "**/ContextRunnable*",
    )
}
