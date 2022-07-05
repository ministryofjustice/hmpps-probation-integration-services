package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.UserService
import javax.annotation.PostConstruct

@Component
class ServiceContext(
    @Value("\${delius.db.username:prison-case-notes-to-probation}") private val deliusDbName: String,
    private val userService: UserService,
) {
    lateinit var servicePrincipal: ServicePrincipal

    @PostConstruct
    fun setUp() {
        val user = userService.findUser(deliusDbName)
        servicePrincipal = ServicePrincipal(deliusDbName, user?.id)
    }
}
