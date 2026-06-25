package uk.gov.justice.digital.hmpps

import com.asyncapi.kotlinasyncapi.springweb.EnableAsyncApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableAsyncApi
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
