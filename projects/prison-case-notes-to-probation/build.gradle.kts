import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

apply(plugin = "com.google.cloud.tools.jib")

dependencies {
    implementation(project(":libs:probation-integration-commons"))

    implementation("org.springframework:spring-jms")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.amazon.sqs)
    implementation(libs.openfeign)
    implementation(libs.sentry)

    dev("org.springframework.boot:spring-boot-starter-activemq")
    dev("com.h2database:h2")
    dev(libs.hawtio)
    dev(libs.wiremock)

    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockito.kotlin)
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**",
        "**/entity/**",
        "**/AppKt.class",
    )
}
