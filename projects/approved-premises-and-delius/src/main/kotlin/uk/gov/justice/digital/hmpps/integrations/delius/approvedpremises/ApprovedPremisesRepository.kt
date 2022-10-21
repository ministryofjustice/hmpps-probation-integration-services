package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises

import org.springframework.data.jpa.repository.JpaRepository

interface ApprovedPremisesRepository : JpaRepository<ApprovedPremises, Long> {
    fun existsByCodeCode(code: String): Boolean
}
