package uk.gov.justice.digital.hmpps.integrations.delius.referral

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi

interface NsiRepository : JpaRepository<Nsi, Long> {
    @EntityGraph(attributePaths = ["person", "status"])
    fun findByPersonCrnAndExternalReference(crn: String, ref: String): Nsi?
}

fun NsiRepository.getByCrnAndExternalReference(crn: String, ref: String) =
    findByPersonCrnAndExternalReference(crn, ref)
        ?: throw NotFoundException("NSI with reference $ref for CRN $crn not found")