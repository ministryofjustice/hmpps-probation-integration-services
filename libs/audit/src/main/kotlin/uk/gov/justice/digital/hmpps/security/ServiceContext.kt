package uk.gov.justice.digital.hmpps.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.user.UserService

@Component
class ServiceContext(
    @Value("\${delius.db.username}") private val deliusDbName: String,
    private val userService: UserService,
) : ApplicationListener<ApplicationStartedEvent> {
    companion object {
        private var servicePrincipal: ServicePrincipal? = null

        fun servicePrincipal() = servicePrincipal
    }

    override fun onApplicationEvent(ase: ApplicationStartedEvent) {
        val user = userService.findUser(deliusDbName) ?: throw IllegalStateException("DB Username Not Found")
        servicePrincipal = ServicePrincipal(deliusDbName, user.id)
    }
}
