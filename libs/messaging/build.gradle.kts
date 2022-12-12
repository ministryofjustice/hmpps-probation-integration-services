import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.insights)
    implementation(libs.sentry)

    api(libs.bundles.aws.messaging)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.mockito)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/exception/**",
        "**/config/**",
    )
}
