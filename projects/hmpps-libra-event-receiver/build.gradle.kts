import uk.gov.justice.digital.hmpps.extensions.ClassPathExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "com.google.cloud.tools.jib")
plugins {
    id("org.unbroken-dome.xjc") version "2.0.0"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://build.shibboleth.net/maven/releases/")
    }
}

dependencies {
    implementation(project(":libs:commons"))
    implementation(project(":libs:oauth-server"))
    implementation(project(":libs:messaging"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.springdoc)

    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.springframework.ws:spring-ws-security:4.1.0") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
    }
    implementation("com.sun.xml.bind:jaxb-impl:4.0.5") {
        exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    implementation("wsdl4j:wsdl4j:1.6.3")
    xjcTool("com.sun.xml.bind:jaxb-xjc:3.0.2")
    xjcTool("com.sun.xml.bind:jaxb-impl:3.0.2")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

    runtimeOnly("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    dev(project(":libs:dev-tools"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.bundles.mockito)
    testImplementation("org.springframework.ws:spring-ws-test")
}

configure<ClassPathExtension> {
    jacocoExclusions = listOf(
        "**/config/**",
        "**/entity/**",
        "**/AppKt.class"
    )
}

xjc {
    srcDirName.set("resources/xsd")
    extension.set(true)
    xjcVersion.set("3.0")
}

sourceSets.named("main") {
    xjcBinding.srcDir("resources/xsd")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}