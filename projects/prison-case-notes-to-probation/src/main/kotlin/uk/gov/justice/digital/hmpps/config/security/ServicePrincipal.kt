package uk.gov.justice.digital.hmpps.config.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.UserService

@Component
class ServicePrincipal(
    @Value("\${delius.db.username}") private val clientId: String,
    private val userService: UserService
) {
    companion object {
        const val AUTHORITY = "ROLE_CASE_NOTES"
    }

    val userId = lazy { userService.findUser(clientId)?.id }
}
