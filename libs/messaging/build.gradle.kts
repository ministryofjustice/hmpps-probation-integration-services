import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.module:jackson-module-kotlin")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    api(libs.bundles.aws.messaging)
    api(libs.asyncapi)

    testImplementation(project(":libs:dev-tools"))
    testImplementation(libs.bundles.mockito)
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/exception/**",
        "**/config/**",
        "**/NotificationHandler.DefaultImpls"
    )
}
