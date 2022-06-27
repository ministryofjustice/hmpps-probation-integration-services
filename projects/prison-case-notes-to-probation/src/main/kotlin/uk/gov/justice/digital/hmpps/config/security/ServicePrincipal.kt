package uk.gov.justice.digital.hmpps.config.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.UserService
import javax.annotation.PostConstruct

@Component
class ServicePrincipal(
    @Value("\${delius.db.username}") val clientId: String,
    private val userService: UserService
) {
    companion object {
        const val AUTHORITY = "ROLE_CASE_NOTES"
    }

    @PostConstruct
    fun postConstruct() {
        userId = userService.findUser(clientId)?.id
    }

    var userId: Long? = 0
}
