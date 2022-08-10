dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    api(libs.wiremock)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}
