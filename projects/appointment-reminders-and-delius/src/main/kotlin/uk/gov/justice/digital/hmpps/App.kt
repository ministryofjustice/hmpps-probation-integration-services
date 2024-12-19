package uk.gov.justice.digital.hmpps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.properties.UpwAppointmentRemindersJobProperties

@SpringBootApplication
@EnableConfigurationProperties(UpwAppointmentRemindersJobProperties::class)
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
