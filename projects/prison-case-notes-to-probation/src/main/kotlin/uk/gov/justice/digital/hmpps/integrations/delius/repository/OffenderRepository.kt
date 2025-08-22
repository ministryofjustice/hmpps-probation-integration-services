package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender

interface OffenderRepository : JpaRepository<Offender, Long> {
    fun findByNomsIdAndSoftDeletedIsFalse(nomsId: String): Offender?
    fun findByCrn(crn: String): Offender?
}

fun OffenderRepository.getByNomsId(nomsId: String) = findByNomsIdAndSoftDeletedIsFalse(nomsId)
    ?: throw OffenderNotFoundException(nomsId)