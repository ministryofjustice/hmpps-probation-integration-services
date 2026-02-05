import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation(libs.bundles.mockito)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/exception/**",
        "**/config/**",
        "**/entity**",
        "**/logging/**"
    )
}
