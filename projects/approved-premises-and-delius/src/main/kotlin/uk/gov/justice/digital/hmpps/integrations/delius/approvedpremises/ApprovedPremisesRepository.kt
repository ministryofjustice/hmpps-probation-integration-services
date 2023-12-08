package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises

interface ApprovedPremisesRepository : JpaRepository<ApprovedPremises, Long> {
    fun existsByCodeCode(code: String): Boolean

    @EntityGraph(attributePaths = ["code", "address", "probationArea"])
    fun findByCodeCodeAndSelectable(
        code: String,
        selectable: Boolean = true,
    ): ApprovedPremises?
}

fun ApprovedPremisesRepository.getApprovedPremises(code: String): ApprovedPremises = findByCodeCodeAndSelectable(code) ?: throw NotFoundException("Approved Premises", "code", code)
