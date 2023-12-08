import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension
dependencies {
    implementation(project(":libs:commons"))
    implementation(project(":libs:messaging"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    api(libs.wiremock)

    testImplementation(libs.bundles.mockito)
}
configure<ClassPathExtension> {
    jacocoExclusions =
        listOf(
            "**/**",
        )
}
