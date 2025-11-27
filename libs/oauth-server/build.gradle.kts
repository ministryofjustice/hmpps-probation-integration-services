dependencies {
    implementation(project(":libs:commons"))
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation(libs.springdoc)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation(libs.bundles.mockito)
}

configure<uk.gov.justice.digital.hmpps.extensions.ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**"
    )
}
