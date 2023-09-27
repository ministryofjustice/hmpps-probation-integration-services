package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.PersonAccess
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.isExcluded
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.isRestricted

@Service
class UserAccessService(private val uar: UserAccessRepository) {
    fun userAccessFor(username: String, crns: List<String>): UserAccess {
        val limitations: Map<String, List<PersonAccess>> = uar.getAccessFor(username, crns).groupBy { it.crn }
        return UserAccess(crns.map { limitations[it].combined(it) })
    }

    private fun List<PersonAccess>?.combined(crn: String): CaseAccess {
        return if (this == null) {
            CaseAccess(crn, userExcluded = false, userRestricted = false)
        } else {
            CaseAccess(
                crn,
                any { it.isExcluded() },
                any { it.isRestricted() },
                firstOrNull { it.isExcluded() }?.exclusionMessage,
                firstOrNull { it.isRestricted() }?.restrictionMessage
            )
        }
    }
}
