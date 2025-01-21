package uk.gov.justice.digital.hmpps

import com.asyncapi.kotlinasyncapi.springweb.EnableAsyncApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.api.model.LaoAccess

@EnableAsyncApi
@EnableConfigurationProperties(LaoAccess::class)
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
