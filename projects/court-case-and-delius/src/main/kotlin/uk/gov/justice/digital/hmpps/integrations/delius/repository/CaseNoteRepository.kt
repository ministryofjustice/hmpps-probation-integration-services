package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.CaseNote

interface CaseNoteRepository : JpaRepository<CaseNote, Long> {
    fun findByExternalReferenceAndOffenderIdAndSoftDeletedIsFalse(
        externalReference: String,
        offenderId: Long,
    ): CaseNote?
}
