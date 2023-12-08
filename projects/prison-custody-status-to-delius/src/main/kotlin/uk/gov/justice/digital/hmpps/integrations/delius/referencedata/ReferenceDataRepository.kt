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

fun ReferenceDataRepository.getReleaseType(code: String): ReferenceData = getByCodeAndSetName(code, "RELEASE TYPE")

fun ReferenceDataRepository.getCustodialStatus(code: String): ReferenceData = getByCodeAndSetName(code, "THROUGHCARE STATUS")

fun ReferenceDataRepository.getCustodyEventType(code: String): ReferenceData = getByCodeAndSetName(code, "CUSTODY EVENT TYPE")

fun ReferenceDataRepository.getTransferStatus(code: String): ReferenceData = getByCodeAndSetName(code, "TRANSFER STATUS")
