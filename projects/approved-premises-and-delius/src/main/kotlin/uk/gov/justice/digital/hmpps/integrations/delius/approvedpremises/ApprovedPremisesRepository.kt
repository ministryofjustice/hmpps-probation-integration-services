package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ApprovedPremisesRepository : JpaRepository<ApprovedPremises, Long> {
    fun existsByCodeCode(code: String): Boolean

    @EntityGraph(attributePaths = ["code", "address"])
    fun findByCodeCode(code: String): ApprovedPremises?
}

fun ApprovedPremisesRepository.getApprovedPremises(code: String): ApprovedPremises =
    findByCodeCode(code) ?: throw NotFoundException("Approved Premises", "code", code)
