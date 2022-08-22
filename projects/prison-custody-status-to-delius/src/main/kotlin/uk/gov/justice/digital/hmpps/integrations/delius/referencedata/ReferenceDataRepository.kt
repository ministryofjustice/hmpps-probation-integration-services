package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndSetNameAndSelectableIsTrue(code: String, set: String): ReferenceData?
}

fun ReferenceDataRepository.getByCodeAndSet(code: String, set: String): ReferenceData =
    findByCodeAndSetNameAndSelectableIsTrue(code, set) ?: throw NotFoundException(set, "code", code)

fun ReferenceDataRepository.getReleaseType(code: String): ReferenceData = getByCodeAndSet(code, "RELEASE TYPE")
fun ReferenceDataRepository.getCustodialStatus(code: String): ReferenceData = getByCodeAndSet(code, "THROUGHCARE STATUS")
fun ReferenceDataRepository.getCustodyEventType(code: String): ReferenceData = getByCodeAndSet(code, "CUSTODY EVENT TYPE")
