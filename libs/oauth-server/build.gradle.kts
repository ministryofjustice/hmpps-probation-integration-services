dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation(libs.springdoc)

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation(libs.bundles.mockito)
}

configure<uk.gov.justice.digital.hmpps.extensions.ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**"
    )
}
