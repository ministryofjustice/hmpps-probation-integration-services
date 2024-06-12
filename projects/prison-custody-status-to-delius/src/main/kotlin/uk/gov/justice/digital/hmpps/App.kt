package uk.gov.justice.digital.hmpps

import org.openfolder.kotlinasyncapi.springweb.EnableAsyncApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementConfigs

@EnableAsyncApi
@EnableConfigurationProperties(PrisonerMovementConfigs::class)
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
