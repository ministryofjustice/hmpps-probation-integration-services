dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.openfeign)
}

configure<uk.gov.justice.digital.hmpps.extensions.ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/FeignConfig*",
        "**/SecurityConfiguration*",
    )
}
