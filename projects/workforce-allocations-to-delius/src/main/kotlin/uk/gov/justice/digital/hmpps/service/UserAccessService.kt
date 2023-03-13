package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserPersonAccess

@Service
class UserAccessService(private val uar: UserAccessRepository) {
    fun userAccessFor(username: String, crns: List<String>): Map<String, UserAccess> {
        val limitations: Map<String, List<UserPersonAccess>> = uar.getAccessFor(username, crns).groupBy { it.crn }
        return crns.associateWith { limitations[it].combined() }
    }

    private fun List<UserPersonAccess>?.combined(): UserAccess {
        return if (this == null) {
            UserAccess.NO_ACCESS_LIMITATIONS
        } else {
            UserAccess(
                any { it.isExcluded() },
                any { it.isRestricted() },
                firstOrNull { it.isExcluded() }?.exclusionMessage,
                firstOrNull { it.isRestricted() }?.restrictionMessage
            )
        }
    }
}
