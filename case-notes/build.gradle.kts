dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-artemis")

    implementation("org.apache.activemq:artemis-plugin:2.22.0")
    implementation("io.hawt:hawtio-springboot:2.15.0")

    runtimeOnly("org.apache.activemq:artemis-jms-server")
}