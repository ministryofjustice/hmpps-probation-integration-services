package uk.gov.justice.digital.hmpps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.ldap.repository.config.EnableLdapRepositories
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUserRepository

@EnableLdapRepositories(basePackageClasses = [LdapUserRepository::class])
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
