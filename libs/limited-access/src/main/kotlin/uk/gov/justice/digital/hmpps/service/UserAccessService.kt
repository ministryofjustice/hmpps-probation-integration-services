package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.LimitedAccessDetail
import uk.gov.justice.digital.hmpps.entity.PersonAccess
import uk.gov.justice.digital.hmpps.entity.UserAccessRepository
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

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

    fun checkLimitedAccessFor(crns: List<String>): UserAccess {
        if (crns.isEmpty()) return UserAccess(emptyList())
        val limitations: Map<String, List<PersonAccess>> = uar.checkLimitedAccessFor(crns).groupBy { it.crn }
        return UserAccess(crns.map { limitations[it].combined(it) })
    }

    fun allCaseAccessForCrn(crn: String): AllCaseAccess {
        val person = uar.findLimitedAccessPersonByCrn(crn)
        val exclusions = uar.getExclusionsForCrn(crn)
        val restrictions = uar.getRestrictionsForCrn(crn)
        return AllCaseAccess(
            crn = crn,
            excludedFrom = exclusions.map {
                LaoDetail(
                    it.username,
                    it.since,
                    it.until
                )
            }.ifEmpty { null },
            restrictedTo = restrictions.map {
                LaoDetail(
                    it.username,
                    it.since,
                    it.until
                )
            }.ifEmpty { null },
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

    fun allCases(page: Pageable): Page<LimitedAccessDetail> {
        return uar.getAll(page).map { row ->
            LimitedAccessDetail(
                crn = row.crn,
                username = row.username,
                type = row.type,
                exclusionMessage = row.exclusionMessage,
                restrictionMessage = row.restrictionMessage,
                startDate = row.startDate.toLimitedAccessDateTime(),
                endDate = row.endDate?.toLimitedAccessDateTime(),
                createdDateTime = row.createdDateTime.toLimitedAccessDateTime(),
                lastUpdatedDateTime = row.lastUpdatedDateTime?.toLimitedAccessDateTime(),
            )
        }
    }

    private fun Any.toLimitedAccessDateTime(): ZonedDateTime = when (this) {
        is ZonedDateTime -> this
        is OffsetDateTime -> toZonedDateTime()
        is Timestamp -> toInstant().atZone(ZoneOffset.UTC)
        is LocalDateTime -> atZone(ZoneOffset.UTC)
        is CharSequence -> toString().toLimitedAccessDateTime()
        else -> asOracleOffsetDateTime()?.toZonedDateTime()
            ?: throw UnsupportedOperationException("Cannot convert ${this::class.qualifiedName} to ZonedDateTime")
    }

    private fun String.toLimitedAccessDateTime(): ZonedDateTime =
        runCatching { OffsetDateTime.parse(this).toZonedDateTime() }
            .recoverCatching { ZonedDateTime.parse(this) }
            .recoverCatching { LocalDateTime.parse(this).atZone(ZoneOffset.UTC) }
            .getOrThrow()

    private fun Any.asOracleOffsetDateTime(): OffsetDateTime? {
        if (javaClass.name != "oracle.sql.TIMESTAMPTZ") return null

        return runCatching {
            javaClass.getMethod("offsetDateTimeValue").invoke(this) as OffsetDateTime
        }.getOrElse {
            throw UnsupportedOperationException("Cannot convert ${this::class.qualifiedName} to OffsetDateTime", it)
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
