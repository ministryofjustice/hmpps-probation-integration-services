package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface OffenderDeltaRepository : JpaRepository<OffenderDelta, Long> {
    @EntityGraph("OffenderDelta.withOffender")
    override fun findAll(pageable: Pageable): Page<OffenderDelta>
}
