package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndSetName(code: String, set: String): ReferenceData?
    fun getByCodeAndSetName(code: String, set: String): ReferenceData =
        findByCodeAndSetName(code, set) ?: throw NotFoundException(set, "code", code)

    fun getV2Tier(tierScore: String) = getByCodeAndSetName("U${tierScore}", "TIER")
    fun getV3Tier(tierScore: String, provisional: Boolean) = when (tierScore) {
        "MISSING" -> "SPM"
        "NOT_SUPERVISED" -> "SPNA"
        else -> "SP${tierScore}${if (provisional) "I" else ""}"
    }.let { getByCodeAndSetName(it, "TIER") }
}