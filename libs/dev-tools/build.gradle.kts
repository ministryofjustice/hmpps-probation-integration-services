import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension
dependencies {
    implementation(project(":libs:commons"))
    implementation(project(":libs:messaging"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    api("org.springframework.boot:spring-boot-starter-artemis")
    api("org.apache.activemq:artemis-jakarta-server")
    api(libs.wiremock)

    testImplementation(libs.mockito.kotlin)
}
configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/**",
    )
}
