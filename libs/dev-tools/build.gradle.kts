import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation(project(":libs:commons"))
    implementation(project(":libs:messaging"))

    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("tools.jackson.module:jackson-module-kotlin")

    api(libs.wiremock)

    testImplementation(libs.bundles.mockito)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/**"
    )
}
