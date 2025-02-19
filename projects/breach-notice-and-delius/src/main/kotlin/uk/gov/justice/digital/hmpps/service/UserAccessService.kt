package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.PersonAccess
import uk.gov.justice.digital.hmpps.integrations.delius.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.isExcluded
import uk.gov.justice.digital.hmpps.integrations.delius.isRestricted
import uk.gov.justice.digital.hmpps.model.CaseAccess
import uk.gov.justice.digital.hmpps.model.UserAccess

@Service
class UserAccessService(private val uar: UserAccessRepository) {
    fun caseAccessFor(username: String, crn: String) =
        userAccessFor(username, listOf(crn)).access.first { it.crn == crn }

    fun userAccessFor(username: String, crns: List<String>): UserAccess {
        val user = uar.findByUsername(username)

        val limitations: List<PersonAccess> =
            user?.let { uar.getAccessFor(it.username, crns) } ?: uar.checkLimitedAccessFor(crns)
        return UserAccess(crns.map { limitations.groupBy { it.crn }[it].combined(it) })
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