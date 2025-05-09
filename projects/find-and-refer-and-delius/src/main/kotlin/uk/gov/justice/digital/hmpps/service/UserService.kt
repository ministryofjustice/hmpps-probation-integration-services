package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import uk.gov.justice.digital.hmpps.entity.isExcluded
import uk.gov.justice.digital.hmpps.entity.isRestricted

@Service
class UserService(private val uar: UserAccessRepository) {

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
