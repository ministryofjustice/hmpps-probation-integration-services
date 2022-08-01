import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.insights)

    dev("com.h2database:h2")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/ThreadConfig*",
        "**/ContextRunnable*",
        "**/TelemetryService*"
    )
}
