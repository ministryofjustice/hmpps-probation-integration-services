import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    api(libs.wiremock)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf()
}
