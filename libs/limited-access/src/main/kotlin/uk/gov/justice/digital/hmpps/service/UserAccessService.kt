package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class UserAccessService(private val uar: UserAccessRepository) {
    companion object {
        private val LONDON: ZoneId = ZoneId.of("Europe/London")
    }
    fun caseAccessFor(username: String, crn: String) =
        userAccessFor(username, listOf(crn)).access.first { it.crn == crn }

    fun userAccessFor(username: String, crns: List<String>): UserAccess {
        val user = uar.findByUsername(username)

        val limitations: List<PersonAccess> =
            user?.let { uar.getAccessFor(it.username, crns) } ?: uar.checkLimitedAccessFor(crns)
        return UserAccess(crns.map { limitations.groupBy { it.crn }[it].combined(it) })
    }

    fun checkLimitedAccessFor(crns: List<String>): UserAccess {
        val limitations: Map<String, List<PersonAccess>> = uar.checkLimitedAccessFor(crns).groupBy { it.crn }
        return UserAccess(crns.map { limitations[it].combined(it) })
    }

    fun allCaseAccessForCrn(crn: String): AllCaseAccess {
        val person = uar.findLimitedAccessPersonByCrn(crn)
        val exclusions = uar.getExclusionsForCrn(crn)
        val restrictions = uar.getRestrictionsForCrn(crn)
        return AllCaseAccess(
            crn = crn,
            excludedFrom = exclusions.map { LaoDetail(it.username, it.start.atStartOfDay(LONDON), it.end?.atZone(LONDON)) }.ifEmpty { null },
            restrictedTo = restrictions.map { LaoDetail(it.username, it.since, it.until) }.ifEmpty { null },
            exclusionMessage = person?.exclusionMessage,
            restrictionMessage = person?.restrictionMessage,
        )
    }

    private fun List<PersonAccess>?.combined(crn: String): CaseAccess {
        return if (this == null) {
            CaseAccess(crn, userExcluded = false, userRestricted = false)
        } else {
            CaseAccess(
                crn,
                any { it.excluded },
                any { it.restricted },
                firstOrNull { it.excluded }?.exclusionMessage,
                firstOrNull { it.restricted }?.restrictionMessage
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

data class AllCaseAccess(
    val crn: String,
    val excludedFrom: List<LaoDetail>?,
    val restrictedTo: List<LaoDetail>?,
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null
)

data class LaoDetail(
    val username: String,
    val since: ZonedDateTime,
    val until: ZonedDateTime? = null,
)
