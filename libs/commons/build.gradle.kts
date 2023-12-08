import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    api(libs.bundles.telemetry)
    compileOnly("org.springframework.boot:spring-boot-starter-data-ldap")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.flipt)

    testImplementation("org.springframework.boot:spring-boot-starter-data-ldap")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.mockito)
}

configure<ClassPathExtension> {
    jacocoExclusions =
        listOf(
            "**/exception/**",
            "**/config/**",
            "**/logging/**",
        )
}
