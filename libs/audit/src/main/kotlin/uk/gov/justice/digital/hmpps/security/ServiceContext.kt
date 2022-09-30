package uk.gov.justice.digital.hmpps.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.user.UserService
import javax.annotation.PostConstruct

@Component
class ServiceContext(
    @Value("\${delius.db.username}") private val deliusDbName: String,
    private val userService: UserService,
) {
    companion object {
        private var servicePrincipal: ServicePrincipal? = null

        fun servicePrincipal() = servicePrincipal
    }

    init {
        servicePrincipal = ServicePrincipal(deliusDbName, null)
    }

    @PostConstruct
    fun setUp() {
        val user = userService.findUser(deliusDbName)
        servicePrincipal = ServicePrincipal(deliusDbName, user?.id)
    }
}
