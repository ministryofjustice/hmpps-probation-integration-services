package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface CaseRepository : JpaRepository<CaseEntity, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): CaseEntity?
}

fun CaseRepository.getCase(crn: String): CaseEntity =
    findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
