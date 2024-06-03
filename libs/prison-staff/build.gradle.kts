import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
