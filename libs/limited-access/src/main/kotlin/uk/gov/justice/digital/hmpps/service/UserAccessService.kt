package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import uk.gov.justice.digital.hmpps.entity.isExcluded
import uk.gov.justice.digital.hmpps.entity.isRestricted

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

data class CaseAccess(
    val crn: String,
    val userExcluded: Boolean,
    val userRestricted: Boolean,
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null
)

data class UserAccess(val access: List<CaseAccess>)
