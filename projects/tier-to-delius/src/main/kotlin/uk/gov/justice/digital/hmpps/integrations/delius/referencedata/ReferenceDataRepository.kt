package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndSetName(
        code: String,
        set: String,
    ): ReferenceData?
}

fun ReferenceDataRepository.getByCodeAndSetName(
    code: String,
    set: String,
): ReferenceData =
    findByCodeAndSetName(code, set) ?: throw NotFoundException(set, "code", code)
