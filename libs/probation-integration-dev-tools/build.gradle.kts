import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension
dependencies {
    implementation(project(":libs:probation-integration-commons"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework:spring-jms")
    implementation(libs.amazon.sqs)
    api(libs.wiremock)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}
configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/**",
    )
}
