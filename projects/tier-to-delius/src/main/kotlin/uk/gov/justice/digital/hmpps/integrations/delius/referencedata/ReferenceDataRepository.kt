package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndSetName(code: String, set: String): ReferenceData?
    fun getByCodeAndSetName(code: String, set: String): ReferenceData =
        findByCodeAndSetName(code, set) ?: throw NotFoundException(set, "code", code)

    fun getV2Tier(tierScore: String) = getByCodeAndSetName("U${tierScore}", "TIER")
    fun getV3Tier(tierScore: String, provisional: Boolean?) =
        getByCodeAndSetName("SP${tierScore}${if (provisional == true) "I" else ""}", "TIER")
}