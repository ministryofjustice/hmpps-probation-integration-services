import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension

dependencies {
    implementation(project(":libs:commons"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.module:jackson-module-kotlin")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    api("com.amazonaws:amazon-sqs-java-extended-client-lib:2.1.2")

    api(platform(libs.aws))
    api("io.awspring.cloud:spring-cloud-aws-autoconfigure")
    api("io.awspring.cloud:spring-cloud-aws-starter-sns")
    api("io.awspring.cloud:spring-cloud-aws-starter-sqs")
    api("io.awspring.cloud:spring-cloud-aws-starter")
    api("software.amazon.awssdk:aws-query-protocol")
    api("software.amazon.awssdk:sts")
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
