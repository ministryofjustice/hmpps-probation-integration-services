package uk.gov.justice.digital.hmpps

import org.openfolder.kotlinasyncapi.springweb.EnableAsyncApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration
import org.springframework.boot.runApplication

@EnableAsyncApi
@SpringBootApplication(exclude = [LdapRepositoriesAutoConfiguration::class])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
