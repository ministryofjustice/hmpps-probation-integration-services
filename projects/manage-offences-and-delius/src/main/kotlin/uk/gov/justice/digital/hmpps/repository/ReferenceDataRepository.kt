package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndSetName(
        code: String,
        set: String,
    ): ReferenceData?
}

fun ReferenceDataRepository.findCourtCategory(code: String) = findByCodeAndSetName(code, "COURT CATEGORY")
