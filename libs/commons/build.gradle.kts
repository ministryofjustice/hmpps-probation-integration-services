import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-jms")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.amazon.sqs)
    implementation(libs.insights)
    implementation(libs.openfeign)
    implementation(libs.sentry)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/AuditingConfig*",
        "**/ContextRunnable*",
        "**/ConnectionProviderConfig*",
        "**/exception/**",
        "**/FeignConfig*",
        "**/JmsConfig*",
        "**/OracleCondition*",
        "**/SecurityConfiguration*",
        "**/ThreadConfig*",
    )
}
