package uk.gov.justice.digital.hmpps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import uk.gov.justice.digital.hmpps.config.AwsConfigProperties

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AwsConfigProperties::class)
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
