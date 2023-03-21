import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

apply(plugin = "com.google.cloud.tools.jib")

dependencies {
    implementation(project(":libs:audit"))
    implementation(project(":libs:commons"))
    implementation(project(":libs:messaging"))
    implementation(project(":libs:oauth-client"))
    implementation(project(":libs:oauth-server"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.openfeign)
    implementation(libs.springdoc)

    dev(project(":libs:dev-tools"))
    dev("com.h2database:h2")

    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.mockito)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**",
        "**/entity/**",
        "**/randm/**",
        "**/AppKt.class"
    )
}
