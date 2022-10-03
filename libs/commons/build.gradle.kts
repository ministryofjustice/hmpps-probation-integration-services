import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-jms")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.amazon.sqs)
    implementation(libs.insights)
    implementation(libs.sentry)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/exception/**",
        "**/config/**",
    )
}
